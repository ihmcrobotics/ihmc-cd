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
import us.ihmc.encryptedProperties.EncryptedPropertyManager

lateinit var LogTools: Logger
val credentials = EncryptedPropertyManager.loadEncryptedCredentials()

class IHMCCDPlugin : Plugin<Project>
{
   override fun apply(project: Project)
   {
      LogTools = project.logger

      val upgrade: (Task) -> Unit = { task ->
         task.doLast {
            // build list of dependencies in this project
            var buildFile = project.file("build.gradle.kts")
            if (!buildFile.exists())
            {
               buildFile = project.file("build.gradle")
            }
            val readText = buildFile.readText()
            val regex = Regex("([\"'][ \\t\\x0B]*us\\.ihmc[ \\t\\x0B\",]*" +
                              "(?:name)??:[ \\t\\x0B\"]*)([0-9a-zA-Z-]{1,50}+)([ \\t\\x0B\",]*" +
                              "(?:version)??:[ \\t\\x0B\"]*)([0-9\\.]+)([ \\t\\x0B]*[\"'])")

            val artifactNameIndex = 2;
            val versionGroupIndex = 4;

            regex.replace(readText) { matchResult ->
               var replacement = ""
               for ((index, groupValue) in matchResult.groupValues.withIndex())
               {
                  LogTools.quiet(groupValue)
                  if (index == versionGroupIndex)
                  {
                     val artifactName = matchResult.groupValues[artifactNameIndex]
                     val latestVersion = queryBintray(artifactName).get("latest_version")
                     LogTools.quiet("[ihmc-cd] Upgrading $artifactName $groupValue -> $latestVersion")
                     replacement += latestVersion
                  }
                  else
                  {
                     replacement += groupValue
                  }
               }
               replacement
            }

            // actually replace the versions

            // TODO use Git to commit and push the changes
         }
      }
      project.tasks.register("upgrade", upgrade)

      val release: (Task) -> Unit = { task ->
         task.doLast {
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
      project.tasks.register("release", release)
   }

   private fun gitStuff(project: Project)
   {
      val git = Git.open(project.projectDir)
      LogTools.quiet("Current branch: {}", git.repository.branch)
   }

   private fun queryBintray(artifactName: String): JSONObject
   {
      val requestBintray = Request.Builder()
            .url("https://api.bintray.com/packages/ihmcrobotics/maven-release/$artifactName")
            .header("Authorization", Credentials.basic(credentials["bintrayUsername"]!!, credentials["bintrayApiKey"]!!))
            .build()
      val client = OkHttpClient()
      val responseBintray = client.newCall(requestBintray).execute()
      val body = responseBintray.body()!!
      val dataBintray = body.string()
      val jsonBintray = JSONObject(dataBintray)
      LogTools.quiet("Bintray data: {}", jsonBintray.toString(3))
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
