import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date


class Aufgabe3 extends Actor with ActorLogging {
  val path = "akka://HFU@127.0.0.1:8002/user/task2"
  val task2Actor= context.actorSelection(path)

  val cluster= Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  def receive() = {
    case "stop" =>
      context.stop(self)
    case message:String =>
      val convertedArray = convertStringToArray(message)
      task2Actor ! Datapoint(convertStringToTimeStamp(convertedArray(0)), convertedArray(2).toFloat)
    case message => println("Actor3: Unhandeled Message: " + message)
  }

  def convertStringToArray(input: String): Array[String] = {
    val convertedArray = input.split(",").map(_.trim)
    convertedArray
  }

  def convertStringToTimeStamp(input: String): Timestamp = {
    val date: Date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(input);
    new Timestamp(date.getTime)

  }
}
