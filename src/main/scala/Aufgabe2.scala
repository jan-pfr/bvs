import akka.actor.{Actor, ActorRef}

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer


case class datapoint(timeStamp:Timestamp, value: Float)
class Aufgabe2(actorRef: ActorRef) extends Actor{

  val values = new ListBuffer[datapoint]
  def receive() = {

    case datapoint(timeStamp, value) =>
      values+=datapoint(timeStamp, value)

      val datapointsFromTheLastDay = new ListBuffer[Float]
      val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)

      for(x <- values) {
        if (testTimePeriod.before(x.timeStamp)){
          datapointsFromTheLastDay+=x.value

        }
      }
      var summe:Float = 0
      for(y <- datapointsFromTheLastDay){
        summe+=y
      }
      summe = summe/datapointsFromTheLastDay.length
      println("Tag: " + timeStamp + ", Mittelwert: " + BigDecimal(summe).setScale(2, BigDecimal.RoundingMode.HALF_UP) + ", Anzahl Datensätze: " + datapointsFromTheLastDay.length)
      actorRef ! collum(timeStamp, summe)
    case "stop" =>
      println("Actor2 stopped.")
      context.stop(self)

    case _ => println("Actor2: Invalid message.")

  }

}
