import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer
case class Datapoint(timeStamp:Timestamp, value: Float)
class Aufgabe2 extends Actor with ActorLogging {
  val path = "akka://HFU@127.0.0.1:2551/user/task1"
  val task1Actor= context.actorSelection(path)
  val cluster= Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  val values = new ListBuffer[Datapoint]
  def receive() = {

    case Datapoint(timeStamp, value) =>
      values+=Datapoint(timeStamp, value)

      val datapointsFromTheLastDay = new ListBuffer[Float]
      val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)

      for(x <- values) {
        if (testTimePeriod.before(x.timeStamp)){
          datapointsFromTheLastDay+=x.value

        }
      }
      var summe:Float = datapointsFromTheLastDay.sum
      summe = summe/datapointsFromTheLastDay.length
      //println("Tag: " + timeStamp + ", Mittelwert: " + BigDecimal(summe).setScale(2, BigDecimal.RoundingMode.HALF_UP) + ", Anzahl Datensätze: " + datapointsFromTheLastDay.length)
      task1Actor ! Collum(timeStamp, summe)
    case "stop" =>
      println("Actor2 stopped.")
      context.stop(self)
    case message => println("Actor2: Unhandeled Message: " + message)

  }

}
