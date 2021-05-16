import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import java.sql.Timestamp
import scala.collection.mutable
case class Datapoint(timeStamp:Timestamp, value: Float)
class Aufgabe2 extends Actor with ActorLogging {
  val path = "akka://HFU@127.0.0.1:2551/user/task1"
  val task1Actor= context.actorSelection(path)
  val cluster= Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

/*
Improvements over the last version of the code:
Instead of a ListBuffer, a mutable queue is now used.
Each time a new datapoint is received, it is added to
the queue and then the complete queue is reduced to the last 24h,
starting from the last received timestmap.
This ensures that the queue is not infinitely large and always the last 24 hours are used.

Source: myself
  */

  val datapointsFromTheLastDay = new mutable.Queue[Datapoint]
  //to be continued
  //val movingAveragePackage = new mutable.Queue[Collum]
  def receive() = {

    case Datapoint(timeStamp, value) =>
      datapointsFromTheLastDay+=Datapoint(timeStamp, value)

      val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)
      datapointsFromTheLastDay.dequeueAll(_.timeStamp.before(testTimePeriod))

      val movingAverage:Float = datapointsFromTheLastDay.map(_.value).sum / datapointsFromTheLastDay.length
      //println("Tag: " + timeStamp + ", Mittelwert: " + BigDecimal(movingAverage).setScale(2, BigDecimal.RoundingMode.HALF_UP) + ", Anzahl Datensätze: " + datapointsFromTheLastDay.length)
      task1Actor ! Collum(timeStamp, movingAverage)
    case "stop" =>
      println("Actor2 stopped.")
      context.stop(self)

    case message => println("Actor2: Unhandeled Message: " + message)

  }


}
