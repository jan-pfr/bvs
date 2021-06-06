import akka.actor.ActorSelection
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.MemberStatus
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
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

class HTTPActor extends dynamicActor with JsonSupport {
  val text = "No matching Dataset"

  implicit val timeOut = new Timeout(1 seconds)

  implicit val system = context.system
  implicit val ec = system.dispatcher

  override def receive() = {
    case "update" =>
      databaseActor match {
        case None => registryActor.get ! "DatabaseActor"
      }

    case MemberUp(member)=>
      register(member)
      getDatabaseActor()

    case state:CurrentClusterState =>
      state.members.filter(_.status==MemberStatus.Up).foreach(register)
      getDatabaseActor()

    case message:Option[ActorSelection] =>
      if(databaseActor == None){
        databaseActor = message
        val route = setRoute()
        startServer(route)
      }

  }

  def setRoute() = {
    val route = pathPrefix("when"){
      (pathEnd & get){
        complete{
          errorMessage(text)
        }
      } ~ (pathPrefix(Segment)){input => pathEnd {
        get {
          val convertedInput = Utils.convertStringToTimeStamp(input.replace('_', ' '))
          try {
            val sendQuestion: Future[Float] = ask(databaseActor.get, convertedInput)(timeOut).mapTo[Float]
            onComplete(sendQuestion) {
              case Success(result) =>
                complete {
                  meanTempSuccess(convertedInput.toString, result)
                }
              case Failure(exception) =>
                complete {
                  meanTempFailure(convertedInput.toString)
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
    route
   }

  def startServer(route: Route) = {
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server up and running http://localhost:8080/\nPress Backspace to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
