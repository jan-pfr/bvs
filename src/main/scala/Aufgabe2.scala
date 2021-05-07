import akka.actor.Actor

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer

case class datapoint(timeStamp:Timestamp, value: Float)
case object Terminate
class Aufgabe2 extends Actor{
  val values = new ListBuffer[datapoint]
  def receive() = {
    case datapoint(timeStamp, value) =>
      values+=datapoint(timeStamp, value)

    case Terminate =>
      val valuesList = values.toList
      println("List: " + valuesList)
      println("Actor2 stopped.")
      context.stop(self)

    case _ => println(" Actor2: Invalid message.")

  }

}
