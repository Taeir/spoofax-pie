package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.vfs.path.PPath
import mb.spoofax.runtime.cfg.SpxCoreConfig
import java.io.Serializable

class CoreBuildOrLoad @Inject constructor(log: Logger) : TaskDef<CoreBuildOrLoad.Input, TransientLangImpl> {
  companion object {
    const val id = "coreBuildOrLoad"
  }

  data class Input(val dir: PPath, val isLangSpec: Boolean) : Serializable {
    constructor(config: SpxCoreConfig) : this(config.dir(), config.isLangSpec)
  }

  val log: Logger = log.forContext(CoreBuild::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): TransientLangImpl {
    val dir = input.dir
    if(input.isLangSpec) {
      buildProject(dir)
      buildLangSpec(dir)
    }
    return loadLangRaw(dir)
  }
}

fun ExecContext.buildOrLoad(input: CoreBuildOrLoad.Input) = requireOutput(CoreBuildOrLoad::class.java, CoreBuildOrLoad.id, input).v
fun ExecContext.buildOrLoad(dir: PPath, isLangSpec: Boolean) = buildOrLoad(CoreBuildOrLoad.Input(dir, isLangSpec))
fun ExecContext.buildOrLoad(input: SpxCoreConfig) = buildOrLoad(CoreBuildOrLoad.Input(input))