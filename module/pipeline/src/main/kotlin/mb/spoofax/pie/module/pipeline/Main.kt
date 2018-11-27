package mb.spoofax.pie.module.pipeline

import com.google.inject.Injector
import com.google.inject.Singleton
import mb.log.slf4j.LogModule
import mb.pie.lang.runtime.PieLangRuntimeModule
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.store.lmdb.withLMDBStore
import mb.pie.taskdefs.guice.withGuiceTaskDefs
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PathSrv
import mb.spoofax.api.SpoofaxFacade
import mb.spoofax.api.StaticSpoofaxFacade
import mb.spoofax.api.module.ModuleManager
import mb.spoofax.legacy.LoadMetaLanguages
import mb.spoofax.legacy.StaticSpoofaxCoreFacade
import mb.spoofax.pie.*
import mb.spoofax.pie.generated.TaskDefsModule_spoofax
import mb.spoofax.pie.processing.DocumentResult
import mb.spoofax.runtime.SpoofaxRuntimeModule
import org.metaborg.core.editor.IEditorRegistry
import org.metaborg.core.editor.NullEditorRegistry
import org.metaborg.spoofax.core.Spoofax
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule
import org.metaborg.spoofax.meta.core.SpoofaxMeta
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  // Read input arguments.
  if(args.size < 2) {
    println("Expected 2 argument, got ${args.size}. Example: java -jar target/example.jar workspace examples")
    exitProcess(1)
  }

  val workspaceDirStr = args[0] // Workspace directory
  val containerDirRelStr = args[1] // Container (project) directory, relative to workspace.
//  val documentFileRelStr = args[2] // Document file, relative to container.

  val (injector, spoofaxPipeline) = initSpoofax()

  // Convert workspace directory to format that PIE can work with.
  val pathSrv = injector.getInstance(PathSrv::class.java)
  val workspaceDir = pathSrv.resolveLocal(workspaceDirStr)
  if(!workspaceDir.exists() || !workspaceDir.isDir) {
    println("Workspace at path $workspaceDir does not exist or is not a directory")
    exitProcess(2)
  }
  val containerDir = workspaceDir.resolve(containerDirRelStr)
  if(!containerDir.exists() || !containerDir.isDir) {
    println("Container (project) at path $containerDir does not exist or is not a directory")
    exitProcess(2)
  }

  // Load the Spoofax Core meta-languages that Spoofax-PIE requires.
  LoadMetaLanguages.loadAll(workspaceDir)
  val pie = initPie(injector)

  loadModules(workspaceDir)

  // Run workspace build incrementally, with top-down executor (since we do not have a list of changed files here).
  val session = pie.topDownExecutor.newSession()
  session.requireInitial(spoofaxPipeline.workspace(workspaceDir))


  val result = session.requireInitial(spoofaxPipeline.container(containerDir, workspaceDir))
  result.documentResults.forEach(::processDocument)

  ModuleManager.getInstance().saveModules()

  // Finally, we clean up our resources.
  pie.close()
  pathSrv.close()
}

/**
 * Function called for handling each document that is processed.
 */
private fun processDocument(d: DocumentResult) {
  val astFile = d.document.parent()!!.resolve(d.document.javaPath.toFile().name + ".ast")
  astFile.outputStream().bufferedWriter().use {
    d.ast?.writeAsString(it, Integer.MAX_VALUE)
  }
}

private fun initSpoofax(): Pair<Injector, SpoofaxPipeline> {
  // Setup Spoofax Core (legacy) through its facade.
  val spoofaxCoreFacade = Spoofax(SpoofaxCoreModule(), SpoofaxExtensionModule())
  val spoofaxCoreMetaFacade = SpoofaxMeta(spoofaxCoreFacade)
  StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade)

  // Setup Spoofax-PIE trough its facade.
  val spoofaxFacade = SpoofaxFacade(
    SpoofaxRuntimeModule(), // Spoofax runtime (implementation)
    LogModule(LoggerFactory.getLogger("root")), // SLF4J logging support
    PieVfsModule(), // PIE VFS support
    PieLangRuntimeModule(), // PIE DSL task definitions
    SpoofaxPieModule(), // Spoofax-PIE support
    SpoofaxPieTaskDefsModule(), // Spoofax-PIE task definitions
    TaskDefsModule_spoofax() // Spoofax-PIE generated task definitions
  )
  val injector = spoofaxFacade.injector
  val spoofaxPipeline = injector.getInstance(SpoofaxPipeline::class.java)
  StaticSpoofaxFacade.init(spoofaxFacade)

  return Pair(injector, spoofaxPipeline)
}

private fun initPie(injector: Injector): PieImpl {
  // Create a PIE instance.
  val pieBuilder = PieBuilderImpl()
  pieBuilder.withGuiceTaskDefs(injector) // Use task definitions from the Spoofax-PIE facade.
  pieBuilder.withLMDBStore(File("target/lmdb")) // Use LMDB persistent storage.
  pieBuilder.withLogger(StreamLogger.verbose()) // Verbose logging for testing purposes.
  return pieBuilder.build()
}

/**
 * Loads modules for the given workspace.
 */
fun loadModules(workspaceDir: PPath) {
  val moduleDir = workspaceDir.resolve("modules")
  if (!moduleDir.exists()) moduleDir.createDirectory()

  ModuleManager.getInstance().setModulesLocation(moduleDir)
  ModuleManager.getInstance().loadModules()
}

/** Spoofax Core (legacy) module to disable IDE editor support, since we are running headless. */
class SpoofaxCoreModule : org.metaborg.spoofax.core.SpoofaxModule() {
  override fun bindEditor() {
    bind(IEditorRegistry::class.java).to(NullEditorRegistry::class.java).`in`(Singleton::class.java)
  }
}
