// -*- mode: scala -*-

import mill._, os._, scalalib._, publish._
import scala.util.Properties

object meta {
  val crossVersions = Seq("2.13.16")

  implicit val wd: Path = pwd

  def nonEmpty(s: String): Option[String] = s.trim() match {
    case v if v.isEmpty => None
    case v => Some(v)
  }

  val versionFromEnv = Properties.propOrNone("PUBLISH_VERSION")
  val gitSha = nonEmpty(proc("git", "rev-parse", "--short", "HEAD").call().out.trim())
  val gitTag = nonEmpty(proc("git", "tag", "-l", "-n0", "--points-at", "HEAD").call().out.trim())
  val publishVersion = (versionFromEnv orElse gitTag orElse gitSha).getOrElse("latest")
}

object dotenv extends Cross[Dotenv](meta.crossVersions)
trait Dotenv extends CrossScalaModule with PublishModule { self =>
  def publishVersion = meta.publishVersion

  override def artifactName = "mill-dotenv"

  def pomSettings = PomSettings(
    description = "mill support for twelve-factor apps. load environment variables from .env",
    organization = "com.github.vic",
    url = "https://github.com/vic/mill-dotenv",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("vic", "mill-dotenv"),
    developers = Seq(
      Developer("vic", "Victor Borja", "https://github.com/vic")
    )
  )

  override def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-scalalib:${mill.main.BuildInfo.millVersion}"
  )

  override def scalacOptions: T[Seq[String]] = super.scalacOptions() ++ Seq("-deprecation")

  object tests extends ScalaTests with TestModule.Utest {
    override def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.7.11") ++ self.compileIvyDeps()
  }
}
