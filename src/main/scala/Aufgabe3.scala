import akka.actor.{Actor, ActorLogging, ActorRef}

class Aufgabe3 (actorRef: ActorRef) extends Actor with ActorLogging {

  def receive() = {
    case "stop" =>
      context.stop(self)
      /* to be continued
    case dataPackages:mutable.Queue[String] =>
      for (dataPackage <- dataPackages){
        println(dataPackage)
        val convertedArray = convertStringToArray(dataPackage)
        actorRef ! Datapoint(convertStringToTimeStamp(convertedArray(0)), convertedArray(2).toFloat)
      }

       */
    case message:String =>
      val convertedArray = convertStringToArray(message)
      actorRef ! Datapoint(Utils.convertStringToTimeStamp(convertedArray(0)), convertedArray(2).toFloat)
    case message => println("Actor3: Unhandeled Message: " + message)
  }

  def convertStringToArray(input: String): Array[String] = {
    val convertedArray = input.split(",").map(_.trim)
    convertedArray
  }


}
