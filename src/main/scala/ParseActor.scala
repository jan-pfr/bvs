import Utils.{DataPackageDataPoint, DataPackageList, DataPoint}
import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable

class ParseActor(actorRef: ActorRef) extends Actor with ActorLogging {
  val dataList = new mutable.ListBuffer[DataPoint]

  def receive() = {

    case "stop" =>
      context.stop(self)

    case dataPackage:DataPackageList =>
      dataPackage.List.foreach(row => dataList += DataPoint(Utils.convertStringToTimeStamp(convertStringToArray(row)(0)), convertStringToArray(row)(2).toFloat))
      actorRef ! DataPackageDataPoint(dataPackage.id, dataList.toList)
      dataList.clear()
    case message => log.info("An unhandled message received: {}", message)
  }

  def convertStringToArray(input: String): Array[String] = {
    val convertedArray = input.split(",").map(_.trim)
    convertedArray
  }

}
