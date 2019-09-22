package us.ihmc.cd;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin

class IHMCCDPlugin : Plugin<Project>
{
   lateinit var project: Project

   override fun apply(project: Project)
   {
      this.project = project
      LogTools = project.logger

      // add deploy task
      project.extensions.add("app", AppExtension(project))
      // add SFTP extension
      project.extensions.add("remote", RemoteExtension())

      project.tasks.register("upgrade", UpgradeTask.configureUpgradeTask())
      project.tasks.register("release", ReleaseTask.configureReleaseTask())
   }
}
