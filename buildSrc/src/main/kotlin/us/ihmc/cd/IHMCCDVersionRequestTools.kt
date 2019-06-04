package us.ihmc.cd

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

fun queryGradlePlugins(artifactName: String)
{
   val requestBintray = Request.Builder()
         .url("https://plugins.gradle.org/m2/us/ihmc/$artifactName")
         .build()
   val client = OkHttpClient()
   val response = client.newCall(requestBintray).execute()
   val body = response.body()!!
   val data = body.string()

   LogTools.quiet(data)



}

fun queryBintray(artifactName: String, bintrayUsername: String, bintrayApiKey: String): JSONObject?
{
   var exists = false
   var choppedArtifactName = artifactName
   var jsonBintray: JSONObject?
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
            LogTools.error("Could not find artifact on Bintray ihmcrobotics/maven-release: {}", artifactName)
            break // artifact not found, but fail softly
         }
      }
      else
      {
         exists = true
      }
   }
   while (!exists)

   if (exists) // for debug only
   {
      val versions = jsonBintray!!.get("versions") as JSONArray

      versions.forEach {
         LogTools.trace("Bintray version: {}", it)
      }
   }

   return jsonBintray
}