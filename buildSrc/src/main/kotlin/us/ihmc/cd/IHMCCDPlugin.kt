package us.ihmc.cd;

import com.github.rjeschke.txtmark.Processor
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.jsoup.Jsoup

lateinit var LogTools: Logger

class IHMCCDPlugin : Plugin<Project>
{
   override fun apply(project: Project)
   {
      LogTools = project.logger

      val release: (Task) -> Unit = { task ->

         task.doLast {
            // print current version
            LogTools.quiet("Version: {}", project.version)

            // print release notes
            // locate CHANGELOG.md
            val changelogFile = project.file("CHANGELOG.md")
            if (!changelogFile.exists())
            {
               throw GradleException("Project must maintain a CHANGELOG.md that adheres to https://keepachangelog.com/en/1.0.0/")
            }

            // extract level 2 headings
            val htmlString = Processor.process(changelogFile)
            LogTools.quiet(htmlString)

            val html = Jsoup.parse(htmlString)

            html.getElementsByTag("h2").eachText().forEach {
               LogTools.quiet(it.toString())
            }

            // print is existing publication on Bintray

            // print current git branch name
         }

      }
      project.tasks.register("release", release)

   }
}
