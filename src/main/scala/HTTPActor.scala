import Utils.JsonSupport
import akka.actor.{ActorSelection, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.MemberStatus
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import java.sql.Timestamp
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

//TODO:
// - What is happening, when an invalid date is given?
// - Maybe an 404 Error, (the Server is going to be online in any case, return a 404 until it gets the routes

class HTTPActor extends DynamicActor with JsonSupport {
  val text = "Missing argument: Timestamp"

  implicit val timeOut = new Timeout(1 seconds)

  implicit val system = context.system
  implicit val ec = system.dispatcher

  override def receive():Receive = {
    case "update" =>
      if(databaseActor == None) {
        registryActor.get ! "DatabaseActor"
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
       startServer(setRoute())
      }
    case message => log.info("An unhandled message received: {}", message)
  }

  def setRoute() = {
    val countRoute:Route = concat(
      pathPrefix("count"){
        get{
          try{
            val getCount:Future[Int] = ask(databaseActor.get, "getCount")(timeOut).mapTo[Int]
            onComplete(getCount){
              case Success(result) =>
                complete{
                  count(result)
                }
              case Failure(exception) =>
                complete{
                  errorMessage("An error occurred while trying to count the number of entries in the database.")
                }
            }
          } catch {
            case exception: Exception =>
              complete {
                errorMessage(exception.getMessage)
              }
          }
        }
      }
    )

    val whenRoute = pathPrefix("when") {
      (pathEnd & get) {
        complete {
          errorMessage(text)
        }
      } ~ (pathPrefix(Segment)) { input =>
        pathEnd {
          get {
            val convertedInput = Utils.convertStringToTimeStamp(input.replace('_', ' '))
            if (convertedInput == new Timestamp(0)) {
              complete {
                errorMessage(text)
              }
            }
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
            } catch {
              case exception: Exception =>
                complete {
                  errorMessage(exception.getMessage)
                }
            }
          }
        }
      }
    }
    concat(countRoute, whenRoute)
  }

  def startServer(route: Route) = {
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    log.info(s"Server up and running http://localhost:8080/")
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}

object HTTPActor extends App{
  val system = Utils.createSystem("httpActor.conf", "HFU")
  system.actorOf(Props[HTTPActor], name = "HTTPActor")
}
