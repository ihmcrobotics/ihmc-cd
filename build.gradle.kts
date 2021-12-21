plugins {
   kotlin("jvm") version "1.5.31"
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.5"
   id("us.ihmc.ihmc-cd")
   id("us.ihmc.log-tools-plugin") version "0.6.3"
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

app.entrypoint("MainEntrypoint", "us.ihmc.cd.MainClass")
app.entrypoint(ihmc.sourceSetProject("test"), "TestEntrypoint", "us.ihmc.cd.TestClass")

val hostname: String by project
val username: String by project

tasks.create("deploy") {
   doLast {
      remote.session(hostname, username) {
         exec("mkdir -p /home/$username/.ihmc")
         exec("rm -rf /home/$username/.ihmc/ihmc-cd-test")
         exec("mkdir -p /home/$username/.ihmc/ihmc-cd-test")
         put(file("src").path, "/home/$username/.ihmc/ihmc-cd-test/testDir2")
         put(file("README.md").path, "/home/$username/.ihmc/ihmc-cd-test/README.md")
         get(file("/home/$username/.ihmc/ihmc-cd-test/README.md").path, "README.md")

         exec("echo hello")
      }
   }
}

// Add an IHMC dependency and run 'gradle upgrade' to test
mainDependencies {
}
