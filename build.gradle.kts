import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

plugins {
   id("us.ihmc.ihmc-build") version "0.15.6"
   id("us.ihmc.ihmc-cd")
   id("us.ihmc.log-tools") version "0.3.1"
   kotlin("jvm") version "1.3.20"
}

subprojects {
   this.apply<KotlinPlatformJvmPlugin>()
}

ihmc {
   group = "us.ihmc"
   version = "0.0"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-cd"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}
