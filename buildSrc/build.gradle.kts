import com.gradle.publish.MavenCoordinates

plugins {
   `kotlin-dsl`
   `java-gradle-plugin`
   `maven-publish`
   id("com.gradle.plugin-publish") version "0.12.0"
}

group = "us.ihmc"
version = "1.14"

repositories {
   mavenCentral()
   jcenter()
   maven{
      url = uri("https://dl.bintray.com/ihmcrobotics/maven-release")
   }
}

dependencies {
   compile("com.github.kittinunf.fuel:fuel:2.0.1")
   compile("org.json:json:20180813")
   compile("com.github.rjeschke:txtmark:0.13")
   compile("org.jsoup:jsoup:1.12.1")
   compile("com.squareup.okhttp3:okhttp:3.11.0")
   compile("org.eclipse.jgit:org.eclipse.jgit:5.3.0.201903130848-r")
   compile("com.hierynomus:sshj:0.27.0")
}

val pluginDisplayName = "IHMC CD"
val pluginDescription = "Gradle plugin for delivering and upgrading software."
val pluginVcsUrl = "https://github.com/ihmcrobotics/ihmc-cd"
val pluginTags = listOf("cd", "continuous", "delivery", "ihmc", "robotics")

gradlePlugin {
   plugins.register(project.name) {
      id = project.group as String + "." + project.name
      implementationClass = "us.ihmc.cd.IHMCCDPlugin"
      displayName = pluginDisplayName
      description = pluginDescription
   }
}

pluginBundle {
   website = pluginVcsUrl
   vcsUrl = pluginVcsUrl
   description = pluginDescription
   tags = pluginTags

   plugins.getByName(project.name) {
      id = project.group as String + "." + project.name
      version = project.version as String
      displayName = pluginDisplayName
      description = pluginDescription
      tags = pluginTags
   }

   mavenCoordinates(closureOf<MavenCoordinates> {
      groupId = project.group as String
      artifactId = project.name
      version = project.version as String
   })
}