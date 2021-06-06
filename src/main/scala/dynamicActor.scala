import akka.actor.{Actor, ActorLogging, ActorSelection, RootActorPath}
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.{Cluster, Member}


abstract class dynamicActor extends Actor with ActorLogging {
  var registryActor:Option[ActorSelection] = None
  var databaseActor:Option[ActorSelection] = None
  val cluster= Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)


   def receive()= {
     case message =>
  }

  def register(member: Member) = {
    if(member.hasRole("RegistryActor")){
      val actor = context.actorSelection(RootActorPath(member.address) / "user" / "RegistryActor")
      registryActor = Some(actor)

    }
  }

  def getDatabaseActor() = {
    if(databaseActor == None) {
      try {
        registryActor.get ! "DatabaseActor"
      } catch {
        case exception: Exception =>
      }
    }
  }

}
