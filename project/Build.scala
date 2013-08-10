import sbt._
import Keys._
import play.Project._
import SbtGruntPlugin._
import SbtKarmaPlugin._

object ApplicationBuild extends Build {

  val appName = "playapingmod"
  val appVersion = "1.0"

  val appDependencies = Seq(
    jdbc,
    anorm)

  val ngBuild = TaskKey[Unit]("ng-build", "Run grunt build and compile in project directory")

  def ngInit = Command.command("ng-init") { state =>
    {
      println("npm install:");
      "npm install" !;
      println("bower install:");
      "bower install" !;
      state
    }
  }

  val main = play.Project(appName, appVersion, appDependencies).settings(
    commands ++= Seq(ngInit),
    playAssetsDirectories <+= baseDirectory / "ng-app",
    //(compile in Compile) <<= (compile in Compile) dependsOn (gruntTask("install"))

    ngBuild <<= (compile in ngBuild) map { _ =>
      "grunt build" !;
    },

    compile in ngBuild <<= (compile in Compile).identity)

}