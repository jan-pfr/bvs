import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable

class ParseActor(actorRef: ActorRef) extends Actor with ActorLogging {
  val dataPointQueue = new mutable.ListBuffer[Datapoint]
  def receive() = {
    case "stop" =>
      context.stop(self)

    case dataPackages:List[String] =>
      dataPackages.foreach(dataPackage =>dataPointQueue += Datapoint(Utils.convertStringToTimeStamp(convertStringToArray(dataPackage)(0)), convertStringToArray(dataPackage)(2).toFloat))
      actorRef ! dataPointQueue.toList
      dataPointQueue.clear()

    case message => println("Actor3: Unhandled Message: " + message)
  }

  def convertStringToArray(input: String): Array[String] = {
    val convertedArray = input.split(",").map(_.trim)
    convertedArray
  }


}
