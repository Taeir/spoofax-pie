package mb.spoofax.pie.legacy

import mb.log.api.Logger
import mb.pie.api.ExecContext
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.FileStampers
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PPathImpl
import mb.spoofax.api.module.LanguageModuleKey
import mb.spoofax.api.module.ModuleManager
import mb.spoofax.api.module.SingleFileModuleImpl
import mb.spoofax.api.module.payload.ASTPayload
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.action.ITransformGoal
import org.metaborg.core.analysis.AnalysisException
import org.metaborg.core.context.IContext
import org.metaborg.core.project.IProject
import org.metaborg.core.syntax.ParseException
import org.metaborg.core.transform.TransformException
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit
import org.spoofax.interpreter.terms.IStrategoTerm

data class ProcessOutput(
  val ast: IStrategoTerm?,
  val outputFile: PPath?,
  val inputFile: PPath
) : Tuple3<IStrategoTerm?, PPath?, PPath>

fun ExecContext.processOne(
  file: PPath,
  project: IProject? = null,
  analyze: Boolean = false,
  transformGoal: ITransformGoal? = null,
  reqFileStamper: FileStamper? = null,
  genFileStamper: FileStamper? = null,
  log: Logger? = null
): ProcessOutput? {
  return processAll(arrayListOf(file), project, analyze, transformGoal, reqFileStamper, genFileStamper, log).firstOrNull()
}

fun ExecContext.processAll(
  files: Iterable<PPath>,
  project: IProject? = null,
  analyze: Boolean = false,
  transformGoal: ITransformGoal? = null,
  reqFileStamper: FileStamper? = null,
  genFileStamper: FileStamper? = null,
  log: Logger? = null
): ArrayList<ProcessOutput> {
  val spoofax = Spx.spoofax()
  // Read input files.
  val inputUnits = files.mapNotNull { file ->
    require(file, reqFileStamper)
    if (!file.exists()) {
      log?.warn("File $file does not exist, skipping")
      null
    } else if (file.isDir) {
      log?.warn("Path $file is a directory, skipping")
      null
    } else {
      val resource = file.fileObject
      val langImpl = spoofax.languageIdentifierService.identify(resource, project)
      if (langImpl == null) {
        log?.warn("Cannot identify language of $file, skipping")
        null
      } else {
        val bytes = file.readAllBytes()
        val text = String(bytes)
        spoofax.unitService.inputUnit(file.fileObject, text, langImpl, null)
      }
    }
  }

  val workspacePath = project?.path?.parent()

  //Create modules for files
  val modules = inputUnits.map { iunit ->
    val path = iunit.source()!!.pPath
    val lang = iunit.langImpl().id()

    val relPath = if (workspacePath != null) workspacePath.relativizeStringFrom(path) else path.toString()
    val modKey = LanguageModuleKey(relPath, lang.id)

    println("TAICO: Identified module \"${modKey.path}\", file=$path, language=$lang, short language=${lang.id}")


    var mod = ModuleManager.getInstance().getModule(modKey)
    if (mod != null) {
      mod
    } else {
      mod = SingleFileModuleImpl(modKey, path)
      //TODO Not sure if the current task should generate these files
      generate(PPathImpl(mod.moduleFile.toPath()), FileStampers.modified)
      mod
    }

  }

  println("TAICO: Adding modules")
  ModuleManager.getInstance().addModules(modules)

  println("TAICO: Saving modules")
  ModuleManager.getInstance().saveModules()

  val langImpls = inputUnits.map { it.langImpl() }.toHashSet()

  println("TAICO: Going to add dependencies on parse tables")
  // Parsing.
  // Require parse table.
  langImpls.forEach { langImpl ->
    val parseFacet = langImpl.facet<SyntaxFacet>(SyntaxFacet::class.java)
    if(parseFacet != null) {
      val parseTableFile = parseFacet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, reqFileStamper)
      }
    }
  }

  println("TAICO: I am going to start parsing everything")
  // Create input units.
  // Do actual parsing.
  val parseUnits = try {
    spoofax.syntaxService.parseAll(inputUnits)
  } catch(e: ParseException) {
    log?.error("Parsing failed unexpectedly", e)
    return arrayListOf()
  }

  //Create parse payloads
  parseUnits.map { it.ast() }.zip(modules).forEach { ASTPayload(it.second, it.first) }

  //TODO TAICO return parse payloads instead?
  if(!analyze && transformGoal == null) {
    println("TAICO: I am not going to continue after the parse step")
    return parseUnits.map {
      ProcessOutput(it.ast(), null, it.input().source()!!.pPath)
    }.toCollection(arrayListOf())
  }

  println("TAICO: Continuing after parse step to do analysis/transformation")

  // Require Stratego runtime files for analysis and transformation.
  langImpls.forEach { langImpl ->
    val strategoRuntimeFacet = langImpl.facet(StrategoRuntimeFacet::class.java)
    if(strategoRuntimeFacet != null) {
      strategoRuntimeFacet.ctreeFiles.forEach<FileObject> {
        require(it.pPath, reqFileStamper)
      }
      strategoRuntimeFacet.jarFiles.forEach<FileObject> {
        require(it.pPath, reqFileStamper)
      }
    }
  }
  // Load project for analysis and transformation.
  project ?: throw RuntimeException("Project must be set if analysis is true or transformGoal is not null")

  // Load context for analysis and transformation.
  val parseUnitsPerContext = run {
    val perContext = hashMapOf<IContext, MutableCollection<ISpoofaxParseUnit>>()
    for(parseUnit in parseUnits) {
      val context = spoofax.contextService.get(parseUnit.source(), project, parseUnit.input().langImpl())
      if(!perContext.containsKey(context)) {
        perContext[context] = mutableListOf(parseUnit)
      } else {
        perContext[context]!!.add(parseUnit)
      }
    }
    perContext
  }

  if (analyze) println("TAICO: Going to analyze the ASTs")
  // Perform analysis
  val analyzeUnitsPerContext = if(analyze) {
    val perContext = hashMapOf<IContext, MutableCollection<ISpoofaxAnalyzeUnit>>()
    parseUnitsPerContext.forEach { (context, parseUnits) ->
      context.write().use { _ ->
        try {
          val analyzeResults = spoofax.analysisService.analyzeAll(parseUnits, context)
          val analyzeUnits = analyzeResults.results()
          if(!perContext.containsKey(context)) {
            perContext[context] = analyzeUnits
          } else {
            perContext[context]!!.addAll(analyzeUnits)
          }
        } catch(e: AnalysisException) {
          log?.error("Analysis failed unexpectedly", e)
          return arrayListOf()
        }
      }
    }
    perContext
  } else {
    null
  }

  if (transformGoal != null) println("TAICO: Going to transform the parsed/analyzed units")

  // Perform transformation
  if(transformGoal != null) {
    try {
      val results = if(analyzeUnitsPerContext != null) {
        analyzeUnitsPerContext.flatMap { (context, analyzeUnits) ->
          context.read().use { _ ->
            spoofax.transformService.transformAllAnalyzed(analyzeUnits, context, transformGoal)
          }
        }
      } else {
        parseUnitsPerContext.flatMap { (context, parseUnits) ->
          context.read().use { _ ->
            spoofax.transformService.transformAllParsed(parseUnits, context, transformGoal)
          }
        }
      }
      return results.flatMap { result ->
        val ast = result.ast()
        val outputs = result.outputs()
        if(ast != null && outputs.count() == 0) {
          listOf(ProcessOutput(ast, null, result.source()!!.pPath))
        } else {
          outputs.map { output ->
            val outputResource = output?.output()
            val writtenFile: PPath?
            writtenFile = if(outputResource != null) {
              generate(outputResource.pPath, genFileStamper)
              outputResource.pPath
            } else {
              null
            }
            ProcessOutput(ast, writtenFile, result.source()!!.pPath)
          }
        }
      }.toCollection(arrayListOf())
    } catch(e: TransformException) {
      log?.error("Transformation failed", e)
      return arrayListOf()
    }
  } else {
    return analyzeUnitsPerContext!!.flatMap { it.value }.map {
      ProcessOutput(it.ast(), null, it.input().source()!!.pPath)
    }.toCollection(ArrayList())
  }
}
