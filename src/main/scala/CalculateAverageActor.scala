import Utils.{DataPackageMap, DataPoint}
import akka.actor.{ActorSelection, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.MemberStatus
import akka.routing.RoundRobinPool

import java.sql.Timestamp
import scala.collection.mutable

class CalculateAverageActor extends DynamicActor {

  val dataPointQueue = new mutable.ListBuffer[DataPoint]
  val dataPackageQueue = new mutable.Queue[DataPackageMap]
  val datasetNames = new mutable.ListBuffer[String]
  val dataPointsFromTheLastDay = new mutable.Queue[DataPoint]
  val listOfTheLastDayQueues = new mutable.ListBuffer[mutable.Queue[DataPoint]]

  override def receive() = {

    case dataPackage: DataPackageMap =>
      log.info("Package received")
      if(datasetNames.toList.contains(dataPackage.id)){
        log.info("Name is here!: {}", dataPackage.id)
        checkForDBActor(dataPackage)
      }else{
        log.info("Name is Not here!: {}", dataPackage.id)
        datasetNames += dataPackage.id
        log.info("A Name has been added: {}", datasetNames.toList.toString())
        listOfTheLastDayQueues += dataPointsFromTheLastDay
        log.info("A 24h List has been added: {}", listOfTheLastDayQueues.toList.toString())
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
    val listOfTheLastDay = listOfTheLastDayQueues.toList(datasetNames.indexOf(dataSetName))
    listOfTheLastDay += DataPoint(timeStamp, value)

    val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)
    listOfTheLastDay.dequeueAll(_.timeStamp.before(testTimePeriod))

    val movingAverage:Float = listOfTheLastDay.map(_.value).sum / listOfTheLastDay.length
    dataPointQueue += DataPoint(timeStamp, movingAverage)
    //log.info("Counter of Datasets: {}, packageLength: {}, DataSet: {}", listOfTheLastDay.length, packageLength, datasetNames.indexOf(dataSetName) )
    if(listOfTheLastDay.length >= packageLength){
      databaseActor.get ! dataPointQueue.toList
      dataPointQueue.clear()
    }
  }
  def dataPackageQueueHandler()={
    dataPackageQueue.toList.foreach(dataPointPackage => dataPointPackage.Map.keys.foreach(x => calculateMovingAverage(x, dataPointPackage.Map(x), dataPointPackage.Map.size, dataPointPackage.id)))
    }

  def checkForDBActor(dataPackage: DataPackageMap) = {
    if(databaseActor == None){
      log.info("Database not ready jet.")
      dataPackageQueue += dataPackage
      log.info("DataPackage has been added to Queue.")
    }else{
      log.info("Database is online.")
      dataPackageQueueHandler()
      dataPackage.Map.keys.foreach(value => calculateMovingAverage(value, dataPackage.Map(value), dataPackage.Map.size, dataPackage.id))
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

