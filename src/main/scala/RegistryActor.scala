import akka.actor.{Actor, ActorLogging, ActorSelection, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}

import scala.collection.mutable

class RegistryActor extends Actor with ActorLogging{

  case class actor(name:String, actorRef: Option[ActorSelection])
  var registry = new mutable.ListBuffer[actor]
  val cluster= Cluster(context.system)
  val roles = List("DatabaseActor", "CalculateAverageActor", "HTTPServer")

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive() = {
    case member:String =>
      registry.foreach(register => if(register.name == member){
        sender ! Some(register.actorRef).get
      })

    case MemberUp(member)=>
      addToRegistry(member)

    case state:CurrentClusterState =>
      state.members.filter(_.status==MemberStatus.Up).foreach(addToRegistry)
  }

  def addToRegistry(member: Member) = {
    roles.foreach(role => if(member.hasRole(role)){
      registry += actor(role, Some(context.actorSelection(RootActorPath(member.address) / "user" / ""+role)))
      sendUpdateToAllRegisteredActors()
    })
  }

  def sendUpdateToAllRegisteredActors() = {
    registry.foreach(register => register.actorRef.get ! "update")

  }
}
