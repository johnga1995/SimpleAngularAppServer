package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import Database._
import play.api.libs.json.{JsValue, Json}
import akka.actor.ActorSystem
import javax.inject._

import play.api._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (actorSystem: ActorSystem)(implicit exec: ExecutionContext)  extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def addUserSimple = Action.async {

    UserDAO.insert(User("email1","Juan", 12))
      .map(x => Ok("User Added:" + x.email))

  }


  def addUser: Action[JsValue] = Action.async(parse.json)  {

    implicit request =>

    var receivedJsonObject = request.body

    val email: String = (receivedJsonObject \ "email").as[String]
    var fullName:String = (receivedJsonObject \ "fullName").as[String]
    var age:String = (receivedJsonObject \ "age").as[String]

    UserDAO.insert(User(email,fullName, age.toInt))
      .map(x => Ok(Json.obj("status" -> "Action Completed Successfully")))


  }

  def deleteUserSimple = Action.async {

    UserDAO.getByEmail(User("email1","", 12)).map {
      case Some(user) => UserDAO.delete(user)
      case None =>  Ok("Not Found :(")
    }.map(x => Ok("User Removed!"))
  }

  def deleteUser : Action[JsValue] = Action.async(parse.json)  {

    implicit request =>

      var receivedJsonObject = request.body

      val email: String = (receivedJsonObject \ "email").as[String]

      UserDAO.delete(User(email,"", 0))
        .map(x => Ok(Json.obj("status" -> "Action Completed Successfully")))


  }

  def getAllUsersSimple = Action {
    Ok("Get all users")
  }


  def getAllUsers: Action[AnyContent] = Action.async {
    implicit request =>
      for {
        allUsers <- UserDAO.getAll()
      } yield Ok(Json.toJson(allUsers))
  }

}
