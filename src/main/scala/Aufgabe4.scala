import akka.actor.{Actor, ActorRef}

class Aufgabe4(actorRef: ActorRef) extends Actor{
  def receive() = {
    case "stop" => context.stop(self)
    case message:String =>
      try {
        val importedCSV = io.Source.fromFile("./ressources/" + message)
        for(line <- importedCSV.getLines().drop(1)){
          actorRef ! line
        }
        
      } catch{
        case exception: Exception => println(exception)
      }
    case _ => println("Actor3: Invalid message.")
  }
}
