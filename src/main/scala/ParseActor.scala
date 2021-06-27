import Utils.{DataPackageList, DataPackageMap}
import akka.actor.{Actor, ActorLogging, ActorRef}

import java.sql.Timestamp
import scala.collection.mutable

class ParseActor(actorRef: ActorRef) extends Actor with ActorLogging {
  val dataMap = new mutable.HashMap[Timestamp,Float]

  def receive() = {

    case "stop" =>
      context.stop(self)

    case dataPackage:DataPackageList =>
      dataPackage.List.foreach(row => dataMap += (Utils.convertStringToTimeStamp(convertStringToArray(row)(0)) -> convertStringToArray(row)(2).toFloat))
      actorRef ! DataPackageMap(dataPackage.id, dataMap.toMap)
      dataMap.clear()
    case message => log.info("An unhandled message received: {}", message)
  }

  def convertStringToArray(input: String): Array[String] = {
    val convertedArray = input.split(",").map(_.trim)
    convertedArray
  }

}
