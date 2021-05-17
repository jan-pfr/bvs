import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import java.sql.Timestamp
import scala.collection.mutable
case class Datapoint(timeStamp:Timestamp, value: Float) // ToDo: replace timeStamp with localDateTime
class CalculateAverageActor extends Actor with ActorLogging {
  val path = "akka://HFU@127.0.0.1:2551/user/task1"
  val task1Actor= context.actorSelection(path)
  val cluster= Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])


  val datapointsFromTheLastDay = new mutable.Queue[Datapoint]
  def receive() = {
    case dataPointPackage:List[Datapoint] =>
      dataPointPackage.foreach(x => calculateMovingAverage(x.timeStamp, x.value))


    case "stop" =>
      println("Actor2 stopped.")
      context.stop(self)

    case message => println("Actor2: Unhandled Message: " + message)


  }
def calculateMovingAverage(timeStamp: Timestamp, value: Float) = {
  datapointsFromTheLastDay+=Datapoint(timeStamp, value)

  val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)
  datapointsFromTheLastDay.dequeueAll(_.timeStamp.before(testTimePeriod))

  val movingAverage:Float = datapointsFromTheLastDay.map(_.value).sum / datapointsFromTheLastDay.length
  task1Actor ! Row(timeStamp, movingAverage)
  //println("Tag: " + timeStamp + ", Mittelwert: " + BigDecimal(movingAverage).setScale(2, BigDecimal.RoundingMode.HALF_UP) + ", Anzahl Datensätze: " + datapointsFromTheLastDay.length)
}

}
