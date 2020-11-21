package us.ihmc.cd

import com.github.rjeschke.txtmark.Processor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

lateinit var LogTools: Logger

data class ApiKey(var username: String = "", var apiKey: String = "")

fun gitStuff(project: Project)
{
   val git = Git.open(project.projectDir)
   LogTools.quiet("Current branch: {}", git.repository.branch)
}

fun parseChangelog(project: Project)
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

fun queryBintray(artifactName: String, apiKey: ApiKey): JSONObject
{
   var exists = false
   var choppedArtifactName = artifactName
   var jsonBintray: JSONObject
   do
   {
      val requestBintray = Request.Builder()
            .url("https://api.bintray.com/packages/ihmcrobotics/maven-release/$choppedArtifactName")
            .header("Authorization", Credentials.basic(apiKey.username, apiKey.apiKey))
            .build()
      val client = OkHttpClient()
      val dataBintray = client.newCall(requestBintray).execute().use { it.body?.string() }
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

fun checkBintrayCredentials(project: Project): ApiKey
{
   val bintrayApiKey = ApiKey()
   project.properties["bintrayUsername"].run {
      if (this != null) bintrayApiKey.username = this as String
      else throw GradleException("Please set bintrayUsername in ~/.gradle/gradle.properties")
   }
   project.properties["bintrayApiKey"].run {
      if (this != null) bintrayApiKey.apiKey = this as String
      else throw GradleException("Please set bintrayApiKey in ~/.gradle/gradle.properties")
   }
   return bintrayApiKey
}