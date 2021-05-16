import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{Failure, Success}


trait FinalCaseClassModel extends DefaultJsonProtocol {
  final case class meanTempSuccess(when: String, what: Float)
  final case class meanTempFailure(when: String)
  final case class errorMessage(message: String)
}


trait JsonSupport extends SprayJsonSupport with FinalCaseClassModel {
  implicit val meanTempWithDateFormat = jsonFormat2(meanTempSuccess.apply)
  implicit val meanTempWithoutDateFormat = jsonFormat1(meanTempFailure.apply)
  implicit val errorMessageFormat = jsonFormat1(errorMessage.apply)
}




class HTTPActor extends Actor with JsonSupport {
  implicit val timeOut = new Timeout(1 seconds)
  val path = "akka://HFU@127.0.0.1:2551/user/task1"
  val task1Actor= context.actorSelection(path)

  implicit val system = context.system
  implicit val ec = system.dispatcher

  override def preStart() = {
    val text = "No matching Dataset"
    val route = pathPrefix("when"){
      (pathEnd & get){
        complete{
          errorMessage(text)
        }
      } ~ (pathPrefix(Segment)){input => pathEnd {
        get {
          try {
            val sendQuestion: Future[Float] = ask(task1Actor, Utils.convertStringToTimeStamp(input.replace('_', ' ')))(timeOut).mapTo[Float]
            onComplete(sendQuestion) {
              case Success(result) =>
                complete {
                  meanTempSuccess(Utils.convertStringToTimeStamp(input.replace('_', ' ')).toString, result)
                }
              case Failure(exception) =>
                complete {
                  meanTempFailure(Utils.convertStringToTimeStamp(input.replace('_', ' ')).toString)
                }
            }
          }catch{
            case exception: Exception =>
              complete{
                errorMessage(exception.getMessage)
              }
          }
        }
      }
      }
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server up and running http://localhost:8080/\nPress Backspace to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }


  def receive() = {
    case _ =>
      println("This Actor should not get messages.")

  }
}
