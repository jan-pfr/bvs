import akka.actor.{ActorSystem, Props}

import java.sql.Timestamp


object Main {
  def main(args: Array[String]): Unit = {
    val actor1 = ActorSystem("HFU").actorOf(Props[Aufgabe1], name= "task1")
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 222 )
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 252 )
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 262 )
    actor1 ! collum(new Timestamp(System.currentTimeMillis()), 272 )
    actor1 ! 3.54 //invalid
    actor1 ! "Hallo Ich bins wieder" //invalid
    actor1 ! Terminate

  }

}
