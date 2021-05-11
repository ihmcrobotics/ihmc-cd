package us.ihmc.cd

import com.github.rjeschke.txtmark.Processor
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

fun queryMavenCentral(artifactName: String): String
{
   var jsonMavenCentral: JSONObject
   val baseUrl = "https://search.maven.org/solrsearch/select"
   val part1 = "?q=g:%22us.ihmc%22+AND+a:%22"
   val part2 = "%22&rows=20&core=gav"
   val requestMavenCentral = Request.Builder()
         .url("$baseUrl$part1$artifactName$part2")
         .build()
   val client = OkHttpClient()
   val dataBintray = client.newCall(requestMavenCentral).execute().use { it.body?.string() }
   jsonMavenCentral = JSONObject(dataBintray)
   LogTools.quiet("Maven Central data: {}", jsonMavenCentral.toString(3))
   if (jsonMavenCentral.getJSONObject("response").getInt("numFound") == 0)
   {
      throw GradleException("Artifact could not be found on Maven Central: $artifactName")
   }

   val versions = jsonMavenCentral.getJSONObject("response").getJSONArray("docs")
   versions.forEach {
      LogTools.quiet("Maven Central version: {}", (it as JSONObject).get("v"))
   }

   var latestVersion = SemanticVersionNumber((versions[0] as JSONObject).getString("v"))
   versions.forEach {
      val version = SemanticVersionNumber((it as JSONObject).getString("v"))
      if (version.compareTo(latestVersion) > 0)
      {
         latestVersion = version
      }
   }

   return latestVersion.get()
}
