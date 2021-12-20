plugins {
   kotlin("jvm") version "1.3.41"
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.4"
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

tasks.create("deploy") {
   doLast {
      remote.session("host", "username") {
         sftp.get("/home/remoteusr/testDir2", "/home/localusr/testDir2")
         sftp.put("/home/localusr/test2.txt", "/home/remoteusr/test2.txt")

         exec("cp /home/remoteusr/test2.txt /home/remoteusr/test3.txt")
         exec("echo hello")
      }
   }
}

// Add an IHMC dependency and run 'gradle upgrade' to test
mainDependencies {
}
