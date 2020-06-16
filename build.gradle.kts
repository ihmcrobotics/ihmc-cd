plugins {
   kotlin("jvm") version "1.3.41"
   id("us.ihmc.ihmc-build") version "0.20.1"
   id("us.ihmc.ihmc-ci") version "5.3"
   id("us.ihmc.ihmc-cd")
   id("us.ihmc.log-tools-plugin") version "0.5.0"
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
