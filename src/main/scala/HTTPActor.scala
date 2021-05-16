import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{Failure, Success}




class HTTPActor extends Actor with DefaultJsonProtocol with SprayJsonSupport{
   implicit val timeOut = new Timeout(1 seconds)
  val path = "akka://HFU@127.0.0.1:2551/user/task1"
  val task1Actor= context.actorSelection(path)
  val cluster= Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  // domain model
  final case class meanTempSuccess(when: String, what: Float)
  final case class meanTempFailure(when: String)

  //formats for unmarshalling and marshalling
  implicit val meanTempWithDateFormat = jsonFormat2(meanTempSuccess.apply)
  implicit val meanTempWithoutDateFormat = jsonFormat1(meanTempFailure.apply)

  implicit val system = context.system
  implicit val materializer = ActorMaterializer
  implicit val ec = system.dispatcher
  onstart()
  def receive() = {
    case message =>
      println(message)

  }
  def onstart () = {
    val errorMessage = """{"status": 404,"message":"No matching Dataset"}"""


    val route = pathPrefix("when"){
      (pathEnd & get){
        complete(HttpEntity(ContentTypes.`application/json`, errorMessage))
      } ~ (pathPrefix(Segment)){input => pathEnd {
        get {
          val sendQuestion: Future[Float] = ask(task1Actor, Utils.convertStringToTimeStamp(input.replace('_',' ')))(timeOut).mapTo[Float]
          onComplete(sendQuestion){
            case Success(result) =>
              complete{
                meanTempSuccess(Utils.convertStringToTimeStamp(input.replace('_',' ')).toString, result)
              }
            case Failure(exception) =>
              complete{
                meanTempFailure(Utils.convertStringToTimeStamp(input.replace('_',' ')).toString)
              }
          }
          /*val result = Await.result(sendQuestion, 1 second)
          complete{
            meanTempSuccess(Utils.convertStringToTimeStamp(input.replace('_',' ')).toString, result)
          }
           */
      }
      }
      }
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
