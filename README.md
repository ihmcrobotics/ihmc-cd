# IHMC CD

Gradle plugin for delivering and upgrading IHMC software.

### Features

- `upgrade` task upgrades your build.gradle with the latest IHMC software versions
- `release` checks CI cerver status, publishes jars, confirms publish on Bintray, pushes tag, merges master, publishes release notes

Relies on the conventions at https://keepachangelog.com/en/1.0.0/

### Download

```kotlin
plugins {
   id("us.ihmc.ihmc-build") version "0.29.3"
   id("us.ihmc.ihmc-cd") version "0.0"
}
```

### User Guide

Work in progress.
