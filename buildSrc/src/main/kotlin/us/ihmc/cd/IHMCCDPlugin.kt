package us.ihmc.cd;

import com.github.rjeschke.txtmark.Processor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.internal.impldep.org.codehaus.plexus.util.xml.Xpp3DomBuilder.build
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

      val release: (Task) -> Unit = { task ->

         task.doLast {
            // print current version
            LogTools.quiet("Version: {}", project.version)

            // print release notes
            parseChangelog(project)

            // print is existing publication on Bintray
            bintrayStuff(project)

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

   private fun bintrayStuff(project: Project)
   {
      val requestBintray = Request.Builder()
            .url("https://api.bintray.com/packages/ihmcrobotics/maven-release/${project.name}")
            .header("Authorization", Credentials.basic(credentials["bintrayUsername"]!!, credentials["bintrayApiKey"]!!))
            .build()
      val client = OkHttpClient()
      val responseBintray = client.newCall(requestBintray).execute()
      val body = responseBintray.body()!!
      val dataBintray = body.string()
      val jsonBintray = JSONObject(dataBintray)
      val versions = jsonBintray.get("versions") as JSONArray

      versions.forEach {
         LogTools.quiet("Bintray version: {}", it)
      }

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
