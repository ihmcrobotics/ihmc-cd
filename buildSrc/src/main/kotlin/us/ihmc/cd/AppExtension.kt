package us.ihmc.cd

import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import java.io.File

class AppExtension(val project: Project)
{
   val javaApplication: JavaApplication

   init
   {
      project.pluginManager.apply(ApplicationPlugin::class.java)
      javaApplication = project.extensions.getByType(JavaApplication::class.java)

      javaApplication.mainClassName = "" // allow for multiple app configuration
   }

   fun entrypoint(applicationName: String, mainClassName: String)
   {
      entrypoint(applicationName, mainClassName, null)
   }

   fun entrypoint(applicationName: String, mainClassName: String, defaultJvmOpts: Iterable<String>? = null)
   {
      val entrypoint = project.tasks.create(mainClassName.substringAfterLast(".").decapitalize(), CreateStartScripts::class.java) {
         this.mainClassName = mainClassName
         this.applicationName = applicationName
         if (defaultJvmOpts != null)
         {
            this.defaultJvmOpts = defaultJvmOpts
         }
         this.outputDir = File(project.buildDir, "scripts")
         this.classpath = project.tasks.getByName<Jar>("jar").outputs.files + project.configurations.getByName("default")
      }
      javaApplication.applicationDistribution.into("bin") {
         from(entrypoint)
      }
   }
}