package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.lang.runtime.util.Tuple2
import mb.pie.vfs.path.PPath
import mb.spoofax.runtime.cfg.SpxCoreConfig
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.action.*
import org.metaborg.core.messages.IMessage
import org.metaborg.core.transform.TransformException
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.unit.AnalyzeContrib
import org.metaborg.spoofax.core.unit.ParseContrib
import org.metaborg.util.iterators.Iterables2
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class LegacyTransform @Inject constructor(
  log: Logger,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<LegacyTransform.Input, ArrayList<LegacyTransform.Output>> {
  val log: Logger = log.forContext(LegacyTransform::class.java)

  companion object {
    const val id = "legacy.Transform"
  }

  data class Input(val config: SpxCoreConfig, val project: PPath, val goal: ITransformGoal, val file: PPath, val ast: IStrategoTerm) : Serializable
  data class Key(val project: PPath, val goal: ITransformGoal, val file: PPath) : Serializable {
    constructor(input: Input) : this(input.project, input.goal, input.file)
  }

  data class Output(val ast: IStrategoTerm?, val outputFile: PPath?, val inputFile: PPath) : Tuple2<IStrategoTerm?, PPath?>

  override val id = Companion.id
  override fun key(input: Input) = Key(input)
  override fun ExecContext.exec(input: Input): ArrayList<Output> {
    val spoofax = Spx.spoofax()
    val langImpl = require(legacyBuildOrLoadLanguage.createTask(input.config)).v

    // Require Stratego runtime files
    val facet = langImpl.facet<StrategoRuntimeFacet>(StrategoRuntimeFacet::class.java)
    if(facet != null) {
      facet.ctreeFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
      facet.jarFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
    }

    // Perform transformation
    val resource = input.file.fileObject
    val project = spoofax.projectService.get(resource) ?: throw ExecException("Cannot transform $resource, it does not belong to a project")
    val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val spoofaxContext = spoofax.contextService.get(resource, project, langImpl)
    val analyzeUnit = spoofax.unitService.analyzeUnit(parseUnit,
      AnalyzeContrib(true, true, true, input.ast, Iterables2.empty<IMessage>(), -1), spoofaxContext)
    spoofaxContext.read().use {
      try {
        val results = spoofax.transformService.transform(analyzeUnit, spoofaxContext, input.goal)
        val outputs = results.flatMap { result ->
          val ast = result.ast()
          result.outputs().map { output ->
            val outputResource = output?.output()
            val outputFile: PPath?
            outputFile = if(outputResource != null) {
              generate(outputResource.pPath)
              outputResource.pPath
            } else {
              null
            }
            Output(ast, outputFile, input.file)
          }
        }
        return ArrayList(outputs)
      } catch(e: TransformException) {
        log.error("Transformation failed", e)
        return ArrayList()
      }
    }
  }
}

class LegacyTransformAll @Inject constructor(
  log: Logger,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<LegacyTransformAll.Input, ArrayList<LegacyTransformAll.Output>> {
  val log: Logger = log.forContext(LegacyTransformAll::class.java)

  companion object {
    const val id = "legacy.TransformAll"
  }

  data class AstFilePair(val ast: IStrategoTerm, val file: PPath) : Tuple2<IStrategoTerm, PPath>
  data class Input(val config: SpxCoreConfig, val project: PPath, val goal: ITransformGoal, val pairs: ArrayList<AstFilePair>) : Serializable
  data class Key(val project: PPath, val goal: ITransformGoal, val files: ArrayList<PPath>) : Serializable {
    constructor(input: Input) : this(input.project, input.goal, input.pairs.map { it.file }.toCollection(ArrayList()))
  }

  data class Output(val ast: IStrategoTerm?, val outputFile: PPath?, val inputFile: PPath) : Tuple2<IStrategoTerm?, PPath?>

  override val id = Companion.id
  override fun key(input: Input) = Key(input)
  override fun ExecContext.exec(input: Input): ArrayList<Output> {
    val spoofax = Spx.spoofax()
    val langImpl = require(legacyBuildOrLoadLanguage.createTask(input.config)).v

    // Require Stratego runtime files
    val facet = langImpl.facet<StrategoRuntimeFacet>(StrategoRuntimeFacet::class.java)
    if(facet != null) {
      facet.ctreeFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
      facet.jarFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
    }

    // Perform transformation
    val project = spoofax.projectService.get(input.project.fileObject)
      ?: throw ExecException("Cannot transform $input.project, it is not a project location")
    val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)
    val analyzeUnits = input.pairs.map { (ast, file) ->
      val resource = file.fileObject
      val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
      val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, ast, Iterables2.empty<IMessage>(), -1))
      spoofax.unitService.analyzeUnit(parseUnit, AnalyzeContrib(true, true, true, ast, Iterables2.empty<IMessage>(), -1), spoofaxContext)
    }
    spoofaxContext.read().use {
      try {
        val results = spoofax.transformService.transformAllAnalyzed(analyzeUnits, spoofaxContext, input.goal)
        val outputs = results.flatMap { result ->
          val ast = result.ast()
          result.outputs().map { output ->
            val outputResource = output?.output()
            val writtenFile: PPath?
            writtenFile = if(outputResource != null) {
              generate(outputResource.pPath)
              outputResource.pPath
            } else {
              null
            }
            Output(ast, writtenFile, result.source()?.pPath!!)
          }
        }
        return ArrayList(outputs)
      } catch(e: TransformException) {
        log.error("Transformation failed", e)
        return ArrayList()
      }
    }
  }
}

fun createCompileGoal() = CompileGoal()
fun createNamedGoal(name: String) = EndNamedGoal(name)
