package mb.spoofax.pie.legacy

import mb.pie.api.*
import mb.pie.vfs.path.PPath
import org.metaborg.core.project.IProject
import org.metaborg.core.project.ISimpleProjectService

typealias TransientProject = OutTransientEquatable<IProject, PPath>

class LegacyLoadProject : TaskDef<PPath, TransientProject> {
  companion object {
    const val id = "legacy.LoadProject"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): TransientProject {
    val spoofax = Spx.spoofax()
    val projLoc = input.fileObject
    var project = spoofax.projectService.get(projLoc)
    if(project == null) {
      project = spoofax.injector.getInstance(ISimpleProjectService::class.java).create(projLoc)
    }
    project!!
    return OutTransientEquatableImpl(project, project.path)
  }
}

val IProject.path get() = this.location().pPath
