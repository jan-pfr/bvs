import akka.actor.Props

object Main extends{
  def main(args: Array[String]): Unit = {
    /*
    * All parts of the code and improvement over the last version was done independently.
    * */

    val actorSystem1 = Utils.createSystem("aufgabe1.conf", "HFU")
    val actorSystem2 = Utils.createSystem("aufgabe2.conf", "HFU")
    val actorSystemHTTP = Utils.createSystem("httpActor.conf", "HFU")
    actorSystem1.actorOf(Props[DatabaseActor], name= "task1")
    val actor2 = actorSystem2.actorOf(Props[CalculateAverageActor], name = "task2")
    val actor3 = actorSystem2.actorOf(Props(new ParseActor(actor2)), name = "task3")
    val actor4 = actorSystem2.actorOf(Props(new ReadFileActor(actor3)), name = "task4")
    actorSystemHTTP.actorOf(Props[HTTPActor], name = "HTTPAktor")
    actor4 ! "jena_shorted.csv"

  }

}
