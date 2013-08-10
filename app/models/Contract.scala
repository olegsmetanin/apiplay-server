package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Contract(id: Pk[Long], folder: String, project: Long, title: String, done: Boolean, dueDate: Option[Date], assignedTo: Option[String])

object Contract {

  // -- Parsers

  /**
   * Parse a Contract from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("contract.id") ~
    get[String]("contract.folder") ~
    get[Long]("contract.project") ~
    get[String]("contract.title") ~
    get[Boolean]("contract.done") ~
    get[Option[Date]]("contract.due_date") ~
    get[Option[String]]("contract.assigned_to") map {
      case id~folder~project~title~done~dueDate~assignedTo => Contract(
        id, folder, project, title, done, dueDate, assignedTo
      )
    }
  }

  // -- Queries

  /**
   * Retrieve a Contract from the id.
   */
  def findById(id: Long): Option[Contract] = {
    DB.withConnection { implicit connection =>
      SQL("select * from contract where id = {id}").on(
        'id -> id
      ).as(Contract.simple.singleOpt)
    }
  }

  /**
   * Retrieve todo contracts for the user.
   */
  def findTodoInvolving(user: String): Seq[(Contract,Project)] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from contract
          join project_member on project_member.project_id = contract.project
          join project on project.id = project_member.project_id
          where contract.done = false and project_member.user_email = {email}
        """
      ).on(
        'email -> user
      ).as(Contract.simple ~ Project.simple map {
        case contract~project => contract -> project
      } *)
    }
  }

  /**
   * Find contracts related to a project
   */
  def findByProject(project: Long): Seq[Contract] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from contract
          where contract.project = {project}
        """
      ).on(
        'project -> project
      ).as(Contract.simple *)
    }
  }

  def findByProjectFolder(projectFolder: String): Seq[Contract] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from contract
          join project on project.id = contract.project
          where project.folder = {projectFolder}
        """
      ).on(
        'projectFolder -> projectFolder
      ).as(Contract.simple *)
    }
  }

  /**
   * Delete a contract
   */
  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("delete from contract where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }

  /**
   * Delete all contract in a folder.
   */
  def deleteInFolder(projectId: Long, folder: String) {
    DB.withConnection { implicit connection =>
      SQL("delete from contract where project = {project} and folder = {folder}").on(
        'project -> projectId, 'folder -> folder
      ).executeUpdate()
    }
  }

  /**
   * Mark a contract as done or not
   */
  def markAsDone(contractId: Long, done: Boolean) {
    DB.withConnection { implicit connection =>
      SQL("update contract set done = {done} where id = {id}").on(
        'id -> contractId,
        'done -> done
      ).executeUpdate()
    }
  }

  /**
   * Rename a folder.
   */
  def renameFolder(projectId: Long, folder: String, newName: String) {
    DB.withConnection { implicit connection =>
      SQL("update contract set folder = {newName} where folder = {name} and project = {project}").on(
        'project -> projectId, 'name -> folder, 'newName -> newName
      ).executeUpdate()
    }
  }

  /**
   * Check if a user is the owner of this contract
   */
  def isOwner(contract: Long, user: String): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select count(contract.id) = 1 from contract
          join project on contract.project = project.id
          join project_member on project_member.project_id = project.id
          where project_member.user_email = {email} and contract.id = {contract}
        """
      ).on(
        'contract -> contract,
        'email -> user
      ).as(scalar[Boolean].single)
    }
  }

  /**
   * Create a Contract.
   */
  def create(contract: Contract, tasks: Seq[Long]): Contract = {
    DB.withConnection { implicit connection =>

      // Get the contract id
      val id: Long = contract.id.getOrElse {
        SQL("select next value for contract_seq").as(scalar[Long].single)
      }

      SQL(
        """
          insert into contract values (
            {id}, {title}, {done}, {dueDate}, {assignedTo}, {project}, {folder}
          )
        """
      ).on(
        'id -> id,
        'folder -> contract.folder,
        'project -> contract.project,
        'title -> contract.title,
        'done -> contract.done,
        'dueDate -> contract.dueDate,
        'assignedTo -> contract.assignedTo
      ).executeUpdate()

       // Add tasks
       tasks.foreach { task =>
         SQL("insert into contract_task values ({cid}, {tid})").on('cid -> id, 'tid -> task).executeUpdate()
       }

      contract.copy(id = Id(id))

    }
  }

}
