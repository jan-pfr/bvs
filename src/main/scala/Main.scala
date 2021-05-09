import akka.actor.{ActorSystem, Props}

import java.sql.Timestamp


object Main {
  def main(args: Array[String]): Unit = {

    val r =  new scala.util.Random()
    val actorSystem = ActorSystem("HFU")
    val actor1 = actorSystem.actorOf(Props[Aufgabe1], name= "task1")
    val actor2 = actorSystem.actorOf(Props( new Aufgabe2(actor1)), name= "task2")
    val actor3 = actorSystem.actorOf(Props(new Aufgabe3(actor2)), name = "task3")
    for (i <- 1 to 10){
      actor1 ! collum(new Timestamp(System.currentTimeMillis()), r.nextInt(100))
    Thread.sleep(100)
    }
    actor1 ! 3.54 //invalid
    actor1 ! "Hallo Ich bins wieder" //invalid
    for(y <- 1 to 10){
      actor2 ! datapoint(new Timestamp(System.currentTimeMillis()), r.nextInt(100))
    Thread.sleep(100)}
    actor1 ! "stop"
    actor2 ! "stop"
      actor3 ! "Ich bin eine Maus!"
      actor3 ! "Ich,bin,32,43,Datef,fdf"


  }

}
