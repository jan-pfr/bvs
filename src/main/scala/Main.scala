import akka.actor.Props

object Main extends{
  def main(args: Array[String]): Unit = {

    val actorSystem1 = Utils.createSystem("aufgabe1.conf", "HFU")
    val actorSystem2 = Utils.createSystem("aufgabe2.conf", "HFU")
    val actorSystemHTTP = Utils.createSystem("aufgabe3.conf", "HFU")
    actorSystem1.actorOf(Props[Aufgabe1], name= "task1")
    val actor2 = actorSystem2.actorOf(Props[Aufgabe2], name = "task2")
    val actor3 = actorSystem2.actorOf(Props(new Aufgabe3(actor2)), name = "task3")
    val actor4 = actorSystem2.actorOf(Props(new Aufgabe4(actor3)), name = "task4")
    actorSystemHTTP.actorOf(Props[HTTPActor], name = "HTTPAktor")
    actor4 ! "jena_shorted.csv"

  }

}
