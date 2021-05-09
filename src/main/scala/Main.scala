import akka.actor.{ActorSystem, Props}


object Main {
  def main(args: Array[String]): Unit = {

    val r =  new scala.util.Random()
    val actorSystem = ActorSystem("HFU")
    val actor1 = actorSystem.actorOf(Props[Aufgabe1], name= "task1")
    val actor2 = actorSystem.actorOf(Props( new Aufgabe2(actor1)), name= "task2")
    val actor3 = actorSystem.actorOf(Props(new Aufgabe3(actor2)), name = "task3")
    val actor4 = actorSystem.actorOf(Props(new Aufgabe4(actor3)), name = "task4")
    actor4 ! "jena_shorted.csv"
    actor1 ! "ausgabe"
  }

}
