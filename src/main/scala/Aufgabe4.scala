import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable

class Aufgabe4 (actorRef: ActorRef)  extends Actor with ActorLogging {
  val dataPackage = new mutable.Queue[String]
  def receive() = {
    case "stop" => context.stop(self)
    case message:String =>
      try {
        val importedCSV = io.Source.fromFile("./ressources/" + message)
        for(line <- importedCSV.getLines().drop(1)) {
          Thread.sleep(1)
          actorRef ! line
        }
        Thread.sleep(10000)

          /* to be continued
          dataPackage +=line
          if(dataPackage.length>=100){
            actorRef ! dataPackage
            dataPackage.clearAndShrink()
            println("Cleared and Shrinked")
          }
        }
        actorRef ! dataPackage
        println("DataPackage: " + dataPackage.length)

           */

      } catch{
        case exception: Exception => println(exception)
      }
    case message => println("Actor4: Unhandeled Message: " + message)
  }
}
