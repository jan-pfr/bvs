import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

class Aufgabe4  extends Actor with ActorLogging {
 val path = "akka://HFU@127.0.0.1:8003/user/task3"
  val task3Actor= context.actorSelection(path)
  val cluster= Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])


  def receive() = {
    case "stop" => context.stop(self)
    case message:String =>
      try {
        val importedCSV = io.Source.fromFile("./ressources/" + message)
        for(line <- importedCSV.getLines().drop(1)){
          Thread.sleep(1)
          task3Actor ! line
        }
        
      } catch{
        case exception: Exception => println(exception)
      }
    case message => println("Actor4: Unhandeled Message: " + message)
  }
}
