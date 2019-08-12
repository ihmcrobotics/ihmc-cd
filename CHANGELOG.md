# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0]
### Features
  - New remote extension that provides SFTP w/ SSH Key and remote execution
  - `release` checks CI cerver status, publishes jars, confirms publish on Bintray, pushes tag, merges master, publishes release notes

### Example

```
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
```

## [0.1]
### Features
  - `upgrade` task upgrades your `build.gradle` to the latest IHMC software versions

[Unreleased]: https://github.com/ihmcrobotics/ihmc-cd/compare/1.0...HEAD
[1.0]: https://github.com/ihmcrobotics/ihmc-cd/compare/0.1...1.0
[0.1]: https://github.com/ihmcrobotics/ihmc-cd/releases/tag/0.1