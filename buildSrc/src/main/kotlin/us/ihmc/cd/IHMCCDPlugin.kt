package us.ihmc.cd;

import com.github.rjeschke.txtmark.Processor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

lateinit var LogTools: Logger

class IHMCCDPlugin : Plugin<Project>
{
   lateinit var project: Project
   var bintrayUsername: String = "unset"
   var bintrayApiKey: String = "unset"

   override fun apply(project: Project)
   {
      this.project = project
      LogTools = project.logger

      val upgrade: (Task) -> Unit = { task ->
         task.doLast {
            checkBintrayCredentials(project)

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
                     val latestVersion = queryBintray(artifactName).get("latest_version")
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
      project.tasks.register("upgrade", upgrade)

      val release: (Task) -> Unit = { task ->
         task.doLast {
            checkBintrayCredentials(project)

            // print current version
            LogTools.quiet("Version: {}", project.version)

            // print release notes
            parseChangelog(project)

            // print is existing publication on Bintray
            queryBintray(project.name)

            // print current git branch name
            gitStuff(project)
         }
      }
//      project.tasks.register("release", release)
   }

   private fun checkBintrayCredentials(project: Project)
   {
      project.properties["bintray_user"].run {
         if (this != null) bintrayUsername = this as String
         else throw GradleException("Please set bintray_user in ~/.gradle/gradle.properties")
      }
      project.properties["bintray_key"].run {
         if (this != null) bintrayApiKey = this as String
         else throw GradleException("Please set bintray_key in ~/.gradle/gradle.properties")
      }
   }

   private fun gitStuff(project: Project)
   {
      val git = Git.open(project.projectDir)
      LogTools.quiet("Current branch: {}", git.repository.branch)
   }

   private fun queryBintray(artifactName: String): JSONObject
   {
      var exists = false
      var choppedArtifactName = artifactName
      var jsonBintray: JSONObject
      do
      {
         val requestBintray = Request.Builder()
               .url("https://api.bintray.com/packages/ihmcrobotics/maven-release/$choppedArtifactName")
               .header("Authorization", Credentials.basic(bintrayUsername, bintrayApiKey))
               .build()
         val client = OkHttpClient()
         val responseBintray = client.newCall(requestBintray).execute()
         val body = responseBintray.body()!!
         val dataBintray = body.string()
         jsonBintray = JSONObject(dataBintray)
         LogTools.trace("Bintray data: {}", jsonBintray.toString(3))
         if (jsonBintray.has("message") && jsonBintray.get("message").toString().contains("was not found"))
         {
            if (choppedArtifactName.contains("-"))
            {
               choppedArtifactName = choppedArtifactName.substringBeforeLast("-")
            }
            else
            {
               throw GradleException("Artifact could not be found on Bintray: $artifactName")
            }
         }
         else
         {
            exists = true
         }
      }
      while (!exists)

      val versions = jsonBintray.get("versions") as JSONArray

      versions.forEach {
         LogTools.trace("Bintray version: {}", it)
      }

      return jsonBintray
   }

   private fun parseChangelog(project: Project)
   {
      // locate CHANGELOG.md
      val changelogFile = project.file("CHANGELOG.md")
      if (!changelogFile.exists())
      {
         throw GradleException("Project must maintain a CHANGELOG.md that adheres to https://keepachangelog.com/en/1.0.0/")
      }

      // extract level 2 headings
      val htmlString = Processor.process(changelogFile)
      LogTools.trace(htmlString)

      val html = Jsoup.parse(htmlString)

      html.getElementsByTag("h2").eachText().forEach {
         LogTools.quiet(it.toString())
      }
   }
}
