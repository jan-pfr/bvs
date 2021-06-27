import akka.actor.{Actor, ActorLogging, ActorSelection, RootActorPath}
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.{Cluster, Member}


abstract class DynamicActor extends Actor with ActorLogging {
  var registryActor:Option[ActorSelection] = None
  var databaseActor:Option[ActorSelection] = None
  val cluster= Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])


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
        case exception: Exception => log.info("Actor not yet registered...")
      }
    }
  }

}
