import akka.actor.{Actor, ActorLogging, ActorSelection, Props, RootActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member, MemberStatus}

import scala.collection.mutable

class RegistryActor extends Actor with ActorLogging{

  case class actor(name:String, actorRef: Option[ActorSelection])
  var registry = new mutable.ListBuffer[actor]
  val cluster= Cluster(context.system)
  val roles = List("DatabaseActor", "CalculateAverageActor", "HTTPActor")

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp], classOf[MemberLeft])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive() = {
    case member:String =>
      registry.foreach(register => if(register.name == member){
        sender ! Some(register.actorRef).get
      })

    case MemberUp(member)=>
      addToRegistry(member)

    case MemberLeft(member) =>
      removeFromRegistry(member)

    case state:CurrentClusterState =>
      state.members.filter(_.status==MemberStatus.Up).foreach(addToRegistry)
  }

  def addToRegistry(member: Member) = {
    roles.foreach(role => if(member.hasRole(role)){
      registry += actor(role, Some(context.actorSelection(RootActorPath(member.address) / "user" / ""+role)))
      sendUpdateToAllRegisteredActors()
    })
  }
  def removeFromRegistry(member: Member) = {
    roles.foreach(role => if(member.hasRole(role)){
      registry -= actor(role, Some(context.actorSelection(RootActorPath(member.address) / "user" / ""+role)))
    })
  }

  def sendUpdateToAllRegisteredActors() = {
    registry.foreach(register => register.actorRef.get ! "update")



  }
}

object RegistryActor extends App{
  val system = Utils.createSystem("registryActor.conf","HFU")
  system.actorOf(Props[RegistryActor], name="RegistryActor")

}
