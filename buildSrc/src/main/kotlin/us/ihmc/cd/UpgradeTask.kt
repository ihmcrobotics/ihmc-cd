package us.ihmc.cd

import org.gradle.api.Task

object UpgradeTask
{
   fun configureUpgradeTask(): (Task) -> Unit
   {
      return { task ->
         task.doLast {
            // build list of dependencies in this project
            var buildFile = project.file("build.gradle.kts")
            if (!buildFile.exists())
            {
               buildFile = project.file("build.gradle")
            }
            val readText = buildFile.readText()

            var writeText = upgradeDependencies(readText)

            val pluginRegex = Regex("([\"'][ \\t\\x0B]*us\\.ihmc[ \\t\\x0B\"',]*" +
                                    "(?:name)??:[ \\t\\x0B\"']*)([0-9a-zA-Z-]{1,50}+)([ \\t\\x0B\"',]*" +
                                    "(?:version)??:[ \\t\\x0B\"']*)([0-9\\.]+)([ \\t\\x0B]*[\"'])")

            // browse https://plugins.gradle.org/m2/us/ihmc/ihmc-build/

            buildFile.writeText(writeText)

            // TODO use Git to commit and push the changes
         }
      }
   }

   private fun upgradeDependencies(readText: String): String
   {
      val regex = Regex("([\"'][ \\t\\x0B]*us\\.ihmc[ \\t\\x0B\"',]*" +
                              "(?:name)??:[ \\t\\x0B\"']*)([0-9a-zA-Z-]{1,50}+)([ \\t\\x0B\"',]*" +
                              "(?:version)??:[ \\t\\x0B\"']*)([0-9\\.]+)([ \\t\\x0B]*[\"'])")

      val artifactNameIndex = 2;
      val versionGroupIndex = 4;

      return regex.replace(readText) { matchResult ->
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
               try
               {
                  val artifactName = matchResult.groupValues[artifactNameIndex]
                  val latestVersion = queryMavenCentral(artifactName)
                  when
                  {
                     latestVersion == groupValue                                              ->
                     {
                        LogTools.info("[ihmc-cd] Up-to-date: $artifactName $groupValue -> $latestVersion")
                        replacement += groupValue
                     }
                     SemanticVersionNumber(latestVersion) < SemanticVersionNumber(groupValue) ->
                     {
                        LogTools.error("[ihmc-cd] Latest version appears to be less than current: $artifactName $groupValue -> $latestVersion")
                        replacement += groupValue
                     }
                     else                                                                     ->
                     {
                        LogTools.quiet("[ihmc-cd] Upgrading $artifactName $groupValue -> $latestVersion")
                        replacement += latestVersion
                     }
                  }
               }
               catch (e: Exception)
               {
                  LogTools.error("[ihmc-cd] ${e.javaClass.simpleName}: ${e.message}")
                  replacement += groupValue
               }
            }
            else
            {
               replacement += groupValue
            }
         }
         replacement
      }
   }
}