import com.gradle.publish.MavenCoordinates
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
   kotlin("jvm") version "1.3.20"
   `java-gradle-plugin`
   id("com.gradle.plugin-publish") version "0.10.0"
}

group = "us.ihmc"
version = "0.0"

repositories {
   mavenCentral()
   jcenter()
   maven{
      url = uri("https://dl.bintray.com/ihmcrobotics/maven-release")
   }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<KotlinJvmCompile> {
   kotlinOptions.jvmTarget = "1.8"
}

dependencies {
   compile(gradleKotlinDsl())
   compile(kotlin("stdlib-jdk8", "1.3.20"))
   compile("com.github.kittinunf.fuel:fuel:2.0.1")
   compile("org.json:json:20180813")
   compile("com.github.rjeschke:txtmark:0.13")
   compile("org.jsoup:jsoup:1.12.1")
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