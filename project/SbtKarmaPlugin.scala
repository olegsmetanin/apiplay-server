import sbt._
import Keys._

object SbtKarmaPlugin extends Plugin {

  private def runKarma(task: String) = {
    val cmd = "karma " + task
    cmd !
  }

  // Karma command

  def karmaCommand = {
    Command.single("karma") { (state: State, task: String) =>
      runKarma(task)

      state
    }
  }

  // Karma task

  def karmaTask(taskName: String) = streams map { (s: TaskStreams) =>
      val retval = runKarma(taskName)
      if (retval != 0) {
        throw new Exception("Karma task %s failed".format(taskName))
      }
  }


  // Expose plugin

  override lazy val settings = Seq(commands += karmaCommand)

}
