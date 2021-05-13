import akka.actor.Props

object Main {
  def main(args: Array[String]): Unit = {

    val actorSystem1 = Utils.createSystem("aufgabe1.conf", "HFU")
    val actorSystem2 = Utils.createSystem("aufgabe2.conf", "HFU")
    val actorSystem3 = Utils.createSystem("aufgabe3.conf", "HFU")
    val actorSystem4 = Utils.createSystem("aufgabe4.conf", "HFU")
    actorSystem1.actorOf(Props[Aufgabe1], name= "task1")
    actorSystem2.actorOf(Props[Aufgabe2], name = "task2")
    actorSystem3.actorOf(Props[Aufgabe3], name = "task3")
    val actor4 = actorSystem4.actorOf(Props[Aufgabe4], name = "task4")
    actor4 ! "jena_shorted.csv"

  }

}
