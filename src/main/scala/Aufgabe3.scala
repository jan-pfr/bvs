import akka.actor.{Actor, ActorRef}

import java.sql.Timestamp


class Aufgabe3(actorRef: ActorRef) extends Actor{
  def receive() = {
    case "stop" => context.stop(self)
    case message:String =>
      println("Message: " + message)
      val convertedArray = convertStringtoArray(message)
      actorRef ! datapoint(Timestamp.valueOf( convertedArray(0)), convertedArray(2).toFloat)
    case _ => println("Actor3: Invalid message.")
  }

  def convertStringtoArray(input: String): Array[String] = {
    val convertedArray = input.split(',')
    convertedArray
  }
}
