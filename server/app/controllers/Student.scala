package controllers

import javax.inject._

import play.api.mvc._
import play.api.i18n._
import models.TaskListInMemoryModel
import play.api.libs.json._
import models._
import play.api.data.Forms._


import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

import shared.SharedMessages
import play.api.mvc._
import play.api.i18n._

@Singleton
class Student @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)(implicit ec: ExecutionContext) 
    extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
  
  private val model = new TaskListDatabaseModel(db)

  implicit val studentDataReads = Json.reads[StudentData]
  //implicit val taskItemWrites = Json.writes[TaskItem]

  def load = Action { implicit request =>
     Ok(views.html.studentLogin())
   }

  def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
    request.body.asJson.map { body =>
      Json.fromJson[A](body) match {
        case JsSuccess(a, path) => f(a)
        case e @ JsError(_) => Future.successful(Redirect(routes.Student.loginStudent()))
      }
    }.getOrElse(Future.successful(Redirect(routes.Student.studentProfile())))
  }
  
  def withSessionName(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("name").map(f).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))
  }

  def withSessionUsername(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("username").map(f).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))
  }

  def withSessionStudentid(f: Int => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("studentId").map(studentId => f(studentId.toInt)).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))
  }


  def loginStudent = Action { implicit request =>
      Ok(views.html.studentLogin())
  }

  // def validateStudent(username: String, password: String) = Action {
  //   Ok(s"$username logged in with $password.")
  // }

    def validateStudent = Action { implicit request =>
    val validVals = request.body.asFormUrlEncoded
     validVals.map { args => 
      val validUsername = args("username").head
      val validPassword = args("password").head
      model.validateStudent(validUsername, validPassword)
      Redirect(routes.Student.studentProfile())
      }.getOrElse(Redirect(routes.Student.loginStudent()))
    }

  def studentProfile = Action { implicit request =>
      Ok(views.html.studentProfile())
  }

  // def getAllCourses() = { ??? }

  // def getMyCourses() = { ??? }

  // def addCourse() = { ??? }

  // def getStudentName () = { ??? }

// Need to have a page of both valid and invalid outcomes. STILL IN PROGRESS with the outcomes.
// It will only lead to the profile funtion.
  // def validateStudentPost() = Action { request =>
  //     val postVals = request.body.asFormUrlEncoded
  //     postVals.map { args => 
  //          val username = args("username").head
  //          val password = args("password").head
  //          //Ok(s"$username logged in with $password.")
  //          Redirect(routes.Student.studentProfile())
  //          }.getOrElse(Redirect(routes.Student.studentLogin())) // This will return the user back 
  //          // to the login page. Ok("Oops"))
  //     }

  //functions below are copied from Lewis from TaskList5.scala
// def validateStudent = Action.async { implicit request =>
//     withJsonBody[UserData] { ud =>
//       model.validateStudent(ud.username, ud.password).map { ouserId =>
//         ouserId match {
//           case Some(userid) =>
//             Ok(Json.toJson(true))
//               .withSession("username" -> ud.username, "userid" -> userid.toString, "csrfToken" -> play.filters.csrf.CSRF.getToken.map(_.value).getOrElse(""))
              
//           case None =>
//             Ok(Json.toJson(false))
//         }
//       }
//     }
//   }

def createStudentUser = Action { implicit request =>
    val createVals = request.body.asFormUrlEncoded
     createVals.map { args => 
      val createName = args("name").head
      val createUsername = args("username").head
      val createPassword = args("password").head
      model.createStudentUser(createName, createUsername, createPassword)
      Redirect(routes.Student.studentProfile())
    }.getOrElse(Redirect(routes.Student.loginStudent()))
  }


  def index = Action { implicit request =>
      val facultyMember = "John"
      val password = 12345
      Ok(views.html.studentLogin())
  }

}