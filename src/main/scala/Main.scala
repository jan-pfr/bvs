import akka.actor.Props

object Main extends{
  def main(args: Array[String]): Unit = {

    val actorSystem1 = Utils.createSystem("aufgabe1.conf", "HFU")
    val actorSystem2 = Utils.createSystem("aufgabe2.conf", "HFU")
    val actorSystemHTTP = Utils.createSystem("httpActor.conf", "HFU")
    val actorSystemRegistry = Utils.createSystem("registryActor.conf","HFU")

    actorSystemRegistry.actorOf(Props[RegistryActor], name="RegistryActor")
    actorSystem1.actorOf(Props[DatabaseActor], name= "DatabaseActor")
    actorSystemHTTP.actorOf(Props[HTTPActor], name = "HTTPActor")

    val actor2 = actorSystem2.actorOf(Props[CalculateAverageActor], name = "CalculateAverageActor")
    val actor3 = actorSystem2.actorOf(Props(new ParseActor(actor2)), name = "ParseActor")
    val actor4 = actorSystem2.actorOf(Props(new ReadFileActor(actor3)), name = "ReadFileActor")
    actor4 ! "jena_shorted.csv"

  }

}
