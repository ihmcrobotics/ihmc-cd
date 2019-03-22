# IHMC CD

Gradle plugin for delivering and upgrading IHMC software.

### Features

- `upgrade` task upgrades your build.gradle with the latest IHMC software versions
- `release` checks CI cerver status, publishes jars, confirms publish on Bintray, pushes tag, merges master, publishes release notes

### Download

```kotlin
plugins {
   id("us.ihmc.ihmc-build") version "0.15.7"
   id("us.ihmc.ihmc-cd") version "0.0"
}
```

### User Guide

Work in progress.
