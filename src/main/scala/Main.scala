import akka.actor.{ActorSystem, Props}

object Main {
  def main(args: Array[String]): Unit = {
    val actor = ActorSystem("HFU").actorOf(Props[SimpleActor], name = "anActor")
    actor ! "hello"

  }

}
