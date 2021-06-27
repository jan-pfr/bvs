import Utils.{DataPackageDataPoint, DataPoint}
import akka.actor.{ActorSelection, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.MemberStatus
import akka.routing.RoundRobinPool

import java.sql.Timestamp
import scala.collection.mutable

class CalculateAverageActor extends DynamicActor {

  val dataPointQueue = new mutable.ListBuffer[DataPoint]
  val dataPackageQueue = new mutable.Queue[DataPackageDataPoint]
  val datasetNames = new mutable.ListBuffer[String]
  val listOfTheLastDayQueues = new mutable.ListBuffer[mutable.Queue[DataPoint]]

  override def receive() = {

    case dataPackage: DataPackageDataPoint =>
      if(datasetNames.toList.contains(dataPackage.id)){
        checkForDBActor(dataPackage)
      }else{
        datasetNames += dataPackage.id
        listOfTheLastDayQueues += new mutable.Queue[DataPoint]
        checkForDBActor(dataPackage)
      }

    case "update" =>
      if(databaseActor == None) {
        registryActor.get ! "DatabaseActor"
      }

    case "stop" => context.stop(self)

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
  def calculateMovingAverage(timeStamp: Timestamp, value: Float, packageLength:Int, dataSetName:String) = {
    val dataPointsFromTheLastDay = listOfTheLastDayQueues(datasetNames.indexOf(dataSetName))
    dataPointsFromTheLastDay += DataPoint(timeStamp, value)

    val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)
    dataPointsFromTheLastDay.dequeueAll(_.timeStamp.before(testTimePeriod))

    val movingAverage:Float = dataPointsFromTheLastDay.map(_.value).sum / dataPointsFromTheLastDay.length
    //log.info("T: {}, Average: {}", timeStamp, movingAverage)
    dataPointQueue += DataPoint(timeStamp, movingAverage)
    if(dataPointQueue.length >= packageLength){
      log.info("Counter of Datasets: {}, packageLength: {}, DataSet: {}", dataPointsFromTheLastDay.length, packageLength, datasetNames.indexOf(dataSetName) )
      databaseActor.get ! dataPointQueue.toList
      dataPointQueue.clear()
    }
  }

  def dataPackageQueueHandler()={
    dataPackageQueue.toList.foreach(dataPointPackage => dataPointPackage.List.foreach(x => calculateMovingAverage(x.timeStamp, x.value, dataPointPackage.List.length, dataPointPackage.id)))
    }

  def checkForDBActor(dataPackage: DataPackageDataPoint) = {
    if(databaseActor == None){
      dataPackageQueue += dataPackage
    }else{
      dataPackageQueueHandler()
      dataPackage.List.foreach(value => calculateMovingAverage(value.timeStamp, value.value, dataPackage.List.length, dataPackage.id))
    }

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

