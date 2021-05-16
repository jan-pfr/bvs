import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable.ListBuffer

class Aufgabe4 (actorRef: ActorRef)  extends Actor with ActorLogging {
  val dataPackage = new ListBuffer[String]
  def receive() = {
    case "stop" => context.stop(self)
    case message:String =>
      try {
        val importedCSV = io.Source.fromFile("./ressources/" + message)
        for(line <- importedCSV.getLines().drop(1)) {
          dataPackage +=line
          if(dataPackage.length>=100){
            actorRef ! dataPackage.toList
            dataPackage.clear()
          }
        }
        actorRef ! dataPackage.toList

      } catch{
        case exception: Exception => println(exception)
      }
    case message => println("Actor4: Unhandled Message: " + message)
  }
}
