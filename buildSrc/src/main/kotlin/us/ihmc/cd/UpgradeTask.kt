package us.ihmc.cd

import org.gradle.api.Task

object UpgradeTask
{
   fun configureUpgradeTask(): (Task) -> Unit
   {
      return { task ->
         task.doLast {
            val bintrayApiKey = checkBintrayCredentials(project)

            // build list of dependencies in this project
            var buildFile = project.file("build.gradle.kts")
            if (!buildFile.exists())
            {
               buildFile = project.file("build.gradle")
            }
            val readText = buildFile.readText()
            val regex = Regex("([\"'][ \\t\\x0B]*us\\.ihmc[ \\t\\x0B\"',]*" +
                                    "(?:name)??:[ \\t\\x0B\"']*)([0-9a-zA-Z-]{1,50}+)([ \\t\\x0B\"',]*" +
                                    "(?:version)??:[ \\t\\x0B\"']*)([0-9\\.]+)([ \\t\\x0B]*[\"'])")

            val artifactNameIndex = 2;
            val versionGroupIndex = 4;

            var writeText = regex.replace(readText) { matchResult ->
               var replacement = ""
               for ((index, groupValue) in matchResult.groupValues.withIndex())
               {
                  if (index == 0) // avoid placing original in the replacement
                  {
                     continue
                  }

                  LogTools.trace(groupValue)
                  if (index == versionGroupIndex)
                  {
                     val artifactName = matchResult.groupValues[artifactNameIndex]
                     val latestVersion = queryBintray(artifactName, bintrayApiKey).get("latest_version")
                     if (latestVersion.equals(groupValue))
                     {
                        LogTools.quiet("[ihmc-cd] Up-to-date: $artifactName $groupValue -> $latestVersion")
                     }
                     else
                     {
                        LogTools.quiet("[ihmc-cd] Upgrading $artifactName $groupValue -> $latestVersion")
                     }
                     replacement += latestVersion
                  }
                  else
                  {
                     replacement += groupValue
                  }
               }
               replacement
            }

            buildFile.writeText(writeText)

            // TODO use Git to commit and push the changes
         }
      }
   }
}