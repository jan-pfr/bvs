import akka.actor.Actor
import java.sql.{DriverManager, SQLException, Statement}
import java.sql.Timestamp
import scala.sys.exit
import scala.util.control.Breaks.{break, breakable}

case class collum(timeStamp:Timestamp, value:Float)
class Aufgabe1 extends Actor {
  var con: java.sql.Connection = null
  val statement = connect()

  def receive() = {
    case collum(timeStamp, value) =>
      val insert = collum(timeStamp = timeStamp, value = value)
      try {
        statement.execute(" insert into onruntime values ('"+insert.timeStamp +"', '"+insert.value+"')")
      }catch{
        case e: Exception => println("Error: " + e)
      }

    case "stop" =>
      con.close()
      println("Actor1 stopped.")
      context.stop(self)

    case _ => println("Actor1: Invalid message.")
  }


  //establish a connection to a local in memory database
  def connect(): Statement = {
    try { // Load Database driver
      Class.forName("org.h2.Driver")
    } catch {
      case e: ClassNotFoundException =>
        println("Could not load SQL driver: " + e)
        exit(-1)
    }

    val retries = 10
    breakable {for (i <- 0 until retries) {
      if (i >= 7) {Thread.sleep(1000)}
      println("Connecting to database...")

      try { // Wait a bit for db to start
        // Connect to database
        con = DriverManager.getConnection("jdbc:h2:./ressources/database")
        println("Successfully connected")
        break
      }catch {
        case sqle: SQLException =>
          println("Failed to connect to database attempt " + i)
          println(sqle.getMessage)
        case ie: InterruptedException =>
          println("Thread interrupted? Should not happen. " + ie)
      }
    }}

    val statement = con.createStatement()
    statement.execute("drop table if exists onruntime")
    statement.execute(" create table onruntime  (timestamp timestamp , data float (10))")
    statement
  }
}


