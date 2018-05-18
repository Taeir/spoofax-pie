package mb.spoofax.runtime.pie.config

import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.lang.runtime.path.read
import mb.pie.vfs.path.PPath
import mb.spoofax.runtime.impl.cfg.LangSpecConfig
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.pie.legacy.parse
import java.io.Serializable

class ParseLangSpecCfg : TaskDef<ParseLangSpecCfg.Input, LangSpecConfig?> {
  companion object {
    const val id = "config.ParseLangSpecCfg"
  }

  data class Input(val configLangCfg: SpxCoreConfig, val file: PPath) : Serializable

  override val id: String = Companion.id
  override fun ExecContext.exec(input: Input): LangSpecConfig? {
    val file = input.file
    val text = read(file) ?: return null
    val (ast, _, _) = parse(input.configLangCfg, text, file)
    if(ast == null) {
      return null
    }
    val dir = file.parent();
    val config = LangSpecConfig.fromTerm(ast, dir)
    return config

  }
}