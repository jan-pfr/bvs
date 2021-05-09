import akka.actor.{Actor, ActorRef}

import java.sql.{Timestamp}
import java.text.SimpleDateFormat
import java.util.Date


class Aufgabe3(actorRef: ActorRef) extends Actor{
  def receive() = {
    case "stop" => context.stop(self)
    case message:String =>
      val convertedArray = convertStringToArray(message)
      actorRef ! datapoint(convertStringToTimeStamp(convertedArray(0)), convertedArray(2).toFloat)
    case _ => println("Actor3: Invalid message.")
  }

  def convertStringToArray(input: String): Array[String] = {
    val convertedArray = input.split(",").map(_.trim)
    convertedArray
  }
  def convertStringToTimeStamp(input: String): Timestamp = {
    val date: Date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(input);
    new java.sql.Timestamp(date.getTime)

  }
}
