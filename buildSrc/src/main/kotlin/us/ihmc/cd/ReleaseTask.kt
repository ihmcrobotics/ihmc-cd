package us.ihmc.cd

import org.gradle.api.Task

object ReleaseTask
{
   fun configureReleaseTask(): (Task) -> Unit
   {
      return { task ->
         task.doLast {
            // print current version
            LogTools.quiet("Version: {}", project.version)

            // print release notes
            parseChangelog(project)

            // print is existing publication on Maven Central
            queryMavenCentral(project.name)

            // print current git branch name
            gitStuff(project)
         }
      }
   }
}