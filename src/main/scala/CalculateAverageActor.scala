import akka.actor.ActorSelection
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.MemberStatus
import java.sql.Timestamp
import scala.collection.mutable

case class Datapoint(timeStamp:Timestamp, value: Float)

class CalculateAverageActor extends dynamicActor {

  val dataPointQueue = new mutable.ListBuffer[Row]
  val dataPointPackageQueue = new mutable.Queue[List[Datapoint]]
  val datapointsFromTheLastDay = new mutable.Queue[Datapoint]

   override def receive() = {
    case dataPointPackage:List[Datapoint] =>
      if(databaseActor == None){
        dataPointPackageQueue += dataPointPackage
      }else if(dataPointPackageQueue.length <= 0){
        dataPointPackage.foreach(x => calculateMovingAverage(x.timeStamp, x.value))
        }else{
        dataPointPackageQueue.toList.foreach(dataPointPackage => dataPointPackage.foreach(x => calculateMovingAverage(x.timeStamp, x.value)))
        if(dataPointQueue.length>=0){
          databaseActor.get ! dataPointQueue.toList
          dataPointQueue.clear()
        }
      }

    case "update" =>
      databaseActor match {
        case None => registryActor.get ! "DatabaseActor"
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
      }

    case message => println("Actor2: Unhandled Message: " + message)
   }
  def calculateMovingAverage(timeStamp: Timestamp, value: Float) = {
    datapointsFromTheLastDay+=Datapoint(timeStamp, value)

    val testTimePeriod: Timestamp = new Timestamp(timeStamp.getTime() - 24*60*60*1001)
    datapointsFromTheLastDay.dequeueAll(_.timeStamp.before(testTimePeriod))

    val movingAverage:Float = datapointsFromTheLastDay.map(_.value).sum / datapointsFromTheLastDay.length
    dataPointQueue += Row(timeStamp, movingAverage)
    if(dataPointQueue.length>=50){
      databaseActor.get ! dataPointQueue.toList
      dataPointQueue.clear()
    }
}
}
