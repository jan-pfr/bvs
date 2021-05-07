import akka.actor.{ActorSystem, Props}

import java.sql.Timestamp


object Main {
  def main(args: Array[String]): Unit = {
    val actor1 = ActorSystem("HFU").actorOf(Props[Aufgabe1], name= "task1")
    val actor2 = ActorSystem("HFU").actorOf(Props[Aufgabe2], name= "task2")
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 222 )
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 252 )
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 262 )
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 272 )
    actor1 ! 3.54 //invalid
    actor1 ! "Hallo Ich bins wieder" //invalid
    actor1 ! Terminat
    actor2 ! datapoint(new Timestamp(System.currentTimeMillis()), 223 )
    actor2 ! datapoint(new Timestamp(System.currentTimeMillis()), 256 )
    actor2 ! datapoint(new Timestamp(System.currentTimeMillis()), 282 )
    actor2 ! datapoint(new Timestamp(System.currentTimeMillis()), 292 )
    actor2 ! Terminate

  }

}
