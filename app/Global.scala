import play.api._

import models._
import anorm._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

/**
 * Initial set of data to be imported
 * in the sample application.
 */
object InitialData {

  def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)

  def insert() = {

    if (User.findAll.isEmpty) {

      Seq(
        User("user@sample.com", "UserName1", "111", true),
        User("user1@sample.com", "UserName2", "111", false),
        User("user2@sample.com", "UserName3", "111", false)).foreach(User.create)

      Seq(
        Project(Id(1), "play2", "crm", "Play 2.0") -> Seq(("user@sample.com", "admins"), ("user1@sample.com", "managers"), ("user2@sample.com", "executors")),
        Project(Id(2), "prj2", "crm", "Play 1.2.4") -> Seq(("user@sample.com", "admins"), ("user1@sample.com", "managers")),
        Project(Id(3), "prj3", "crm", "Website") -> Seq(("user@sample.com", "admins"), ("user1@sample.com", "executors")),
        Project(Id(4), "prj4", "crm", "Secret project") -> Seq(("user@sample.com", "managers"), ("user1@sample.com", "executors"), ("user2@sample.com", "executors")),
        Project(Id(5), "prj5", "crm", "Playmate") -> Seq(("user1@sample.com", "admins")),
        Project(Id(6), "prj6", "crm", "Things to do") -> Seq(("user2@sample.com", "admins"))).foreach {
          case (project, members) => Project.create(project, members)
        }

      val tasks = Seq(
        Task(Id(1), "Todo", 1, "Fix the documentation", false, None, Some("user@sample.com")),
        Task(Id(2), "Urgent", 1, "Prepare the beta release", false, Some(date("2011-11-15")), None),
        Task(Id(3), "Todo", 2, "Buy some milk", false, None, None),
        Task(Id(4), "Todo", 2, "Check 1.2.4-RC2", false, Some(date("2011-11-18")), Some("user1@sample.com")),
        Task(Id(5), "Todo", 6, "Finish zentask integration", true, Some(date("2011-11-15")), Some("user2@sample.com")),
        Task(Id(6), "Todo", 4, "Release the secret project", false, Some(date("2012-01-01")), Some("user1@sample.com")))

      tasks.foreach(Task.create)

      Seq(
        Contract(NotAssigned, "Todo", 1, "Fix the documentation", false, None, Some("user@sample.com")) -> Seq(1L),
        Contract(NotAssigned, "Urgent", 1, "Prepare the beta release", false, Some(date("2011-11-15")), None) -> Seq(2L),
        Contract(NotAssigned, "Todo", 1, "Buy some milk", false, None, None) -> Seq()).foreach {
          case (contract, tasks) => Contract.create(contract, tasks)
        }

    }
  }
}