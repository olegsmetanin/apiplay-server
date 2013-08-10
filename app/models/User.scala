package models

import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

case class User(email: String, name: String, password: String, isAdmin: Boolean)
case class UserInfo(email: String, name: String, isAdmin: Boolean)

object UserInfo {
  /**
   * Serialize a UserInfo to json
   */
  implicit val userInfoWrites = Json.writes[UserInfo]  
}

object User {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
    get[String]("user.name") ~
    get[String]("user.password") ~
    get[Boolean]("user.is_admin") map {
      case email~name~password~isAdmin => User(email, name, password, isAdmin)
    }
  }


  /**
   * Serialize a User to UserInfo DTO
   */
  def userInfo(u: User): UserInfo = {
    UserInfo(u.email, u.name, u.isAdmin)
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where email = {email}").on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user").as(User.simple *)
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {email}, {name}, {password}, {is_admin}
          )
        """
      ).on(
        'email -> user.email,
        'name -> user.name,
        'password -> user.password,
        'is_admin -> user.isAdmin
      ).executeUpdate()

      user

    }
  }

}
