package mb.spoofax.pie.style

import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.runtime.style.Styler
import mb.spoofax.runtime.style.StylingRules
import java.io.Serializable

class SpoofaxStyle : TaskDef<SpoofaxStyle.Input, Styling> {
  companion object {
    const val id = "spoofax.Style"
  }

  data class Input(val tokens: ArrayList<Token>, val rules: StylingRules) : Serializable

  override val id = Companion.id
  override fun key(input: Input) = input.tokens
  override fun ExecContext.exec(input: Input): Styling {
    val styler = Styler(input.rules)
    return styler.style(input.tokens)
  }
}
