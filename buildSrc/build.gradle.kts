import com.gradle.publish.MavenCoordinates

plugins {
   `kotlin-dsl`
   `java-gradle-plugin`
   `maven-publish`
   id("com.gradle.plugin-publish") version "0.18.0"
}

group = "us.ihmc"
version = "1.25"

repositories {
   mavenCentral()
   maven {
      url = uri("https://jitpack.io")
   }
}

dependencies {
   api("com.github.kittinunf.fuel:fuel:2.3.1")
   api("org.json:json:20211205")
   api("com.github.rjeschke:txtmark:0.13")
   api("org.jsoup:jsoup:1.14.3")
   api("com.squareup.okhttp3:okhttp:4.9.3")
   api("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r") // newer versions not compatible with Java 8
   api("com.hierynomus:sshj:0.32.0")
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