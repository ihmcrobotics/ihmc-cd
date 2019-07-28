package us.ihmc.cd

import org.gradle.api.Task

object ReleaseTask
{
   fun configureReleaseTask(): (Task) -> Unit
   {
      return { task ->
         task.doLast {
            val bintrayApiKey = checkBintrayCredentials(project)

            // print current version
            LogTools.quiet("Version: {}", project.version)

            // print release notes
            parseChangelog(project)

            // print is existing publication on Bintray
            queryBintray(project.name, bintrayApiKey)

            // print current git branch name
            gitStuff(project)
         }
      }
   }
}