
import Utils.DataPackageList
import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable.ListBuffer

class ReadFileActor(actorRef: ActorRef)  extends Actor with ActorLogging {
  val dataPackageValues = new ListBuffer[String]
  def receive() = {

    case message:String =>
      log.info("Read File: {}", message)
      try {

        val importedCSV = io.Source.fromFile("./ressources/" + message)

        for(line <- importedCSV.getLines()) {
          dataPackageValues +=line
          if(dataPackageValues.length >= 500){
            actorRef ! DataPackageList(message, dataPackageValues.toList)
            dataPackageValues.clear()
          }
        }
        actorRef ! DataPackageList(message, dataPackageValues.toList)

      } catch{
        case exception: Exception => log.info("An Error occured: {}", exception)
      }
    case "stop" => context.stop(self)

    case message => log.info("An unhandled message received: {}", message)
  }
}
