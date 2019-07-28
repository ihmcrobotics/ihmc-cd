package us.ihmc.cd;

import org.gradle.api.Plugin
import org.gradle.api.Project

class IHMCCDPlugin : Plugin<Project>
{
   lateinit var project: Project

   override fun apply(project: Project)
   {
      this.project = project
      LogTools = project.logger

      project.tasks.register("upgrade", UpgradeTask.configureUpgradeTask())
      project.tasks.register("release", ReleaseTask.configureReleaseTask())
   }
}
