import Utils.{DataPackageMap, Datapoint}
import akka.actor.{ActorSelection, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.MemberStatus
import akka.routing.RoundRobinPool

import java.sql.Timestamp
import scala.collection.mutable

class CalculateAverageActor extends DynamicActor {

  val dataPointQueue = new mutable.ListBuffer[Datapoint]
  val dataPackageQueue = new mutable.Queue[DataPackageMap]
  val datapointsFromTheLastDay = new mutable.Queue[Datapoint]

  override def receive() = {
    case dataPackage: DataPackageMap =>
      if(databaseActor == None){
        dataPackageQueue += dataPackage
      }else{
        dataPackageQueueHandler()
        dataPackage.Map.keys.foreach(value => calculateMovingAverage(value, dataPackage.Map(value), dataPackage.Map.size))
      }

    case "update" =>
      if(databaseActor == None) {
        registryActor.get ! "DatabaseActor"
      }

    case "stop" =>
      log.info("Actor2 stopped.")
      context.stop(self)

    case MemberUp(member)=>
      register(member)
      getDatabaseActor()

    case state:CurrentClusterState =>
      state.members.filter(_.status==MemberStatus.Up).foreach(register)
      getDatabaseActor()

    case message:Option[ActorSelection] =>
      if(databaseActor == None){
        databaseActor = message
        dataPackageQueueHandler()
      }

    case message => log.info("An unhandled message received: {}", message)
  }
  def calculateMovingAverage(timeStamp: Timestamp, value: Float, packageLength:Int) = {
    datapointsFromTheLastDay+=Datapoint(timeStamp, value)

    val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)
    datapointsFromTheLastDay.dequeueAll(_.timeStamp.before(testTimePeriod))

    val movingAverage:Float = datapointsFromTheLastDay.map(_.value).sum / datapointsFromTheLastDay.length
    dataPointQueue += Datapoint(timeStamp, movingAverage)
    if(dataPointQueue.length >= packageLength){
      databaseActor.get ! dataPointQueue.toList
      dataPointQueue.clear()
    }
  }
  def dataPackageQueueHandler()={
    dataPackageQueue.toList.foreach(dataPointPackage => dataPointPackage.Map.keys.foreach(x => calculateMovingAverage(x, dataPointPackage.Map(x), dataPointPackage.Map.size)))
    }
}

object CalculateAverageActor extends App {
  val system = Utils.createSystem("CalculateActor.conf", "HFU")
  val calculateAverageActor = system.actorOf(Props[CalculateAverageActor], name = "CalculateAverageActor")

  val parseActor = system.actorOf(Props(new ParseActor(calculateAverageActor)), name = "ParseActor")
  val readFileActorProps = Props(new ReadFileActor(parseActor))
  val routerActor = system.actorOf(RoundRobinPool(2).props(readFileActorProps))
  routerActor ! "jena_head.csv"
  routerActor ! "jena_tail.csv"

}

