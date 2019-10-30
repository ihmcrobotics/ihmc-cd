package us.ihmc.cd;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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

      UpgradeTask.configureUpgradeTask().invoke(project.getOrCreate("upgrade"))
      ReleaseTask.configureReleaseTask().invoke(project.getOrCreate("release"))
   }

   private fun Project.getOrCreate(taskName: String): Task
   {
      return tasks.findByName(taskName) ?: tasks.create(taskName)
   }
}
