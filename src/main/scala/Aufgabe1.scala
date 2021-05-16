import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp}

import java.sql._
import scala.sys.exit
import scala.util.control.Breaks.{break, breakable}

case class Collum(timeStamp:Timestamp, value:Float)

class Aufgabe1 extends Actor with ActorLogging {

  //was tut das?
  val cluster= Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  val preparedInsertStatement = "insert into onruntime values(?,?)"
  val preparedSelectStatement = "select data from onruntime where timestamp= ?"
  var con: java.sql.Connection = null
  val statement = connect()

  def receive() = {
    case Collum(timeStamp, value) =>
      try {
        ExecutePreparedInsertLStatement(preparedInsertStatement, con, timeStamp, value)
      }catch{
        case e: Exception => println("Error: " + e)
      }

    case "stop" =>
      con.close()
      println("Actor1 stopped.")
      context.stop(self)

    case timestampRequest: Timestamp =>
      try {
       val result = ExecutePreparedSelectStatement(preparedSelectStatement, con, timestampRequest)
        while(result.next()){
          val resultData = result.getFloat("data")
          sender ! resultData
        }
      }catch{
        case e: Exception => println("ErrorWhileSelect: " + e)
      }




    case message => println("Actor1: Unhandled Message: " + message)
    case _: MemberEvent => // ignore
  }


  //establish a connection to a local in memory database
  def connect(): Statement = {
    try { // Load Database driver
      Class.forName("org.h2.Driver")
    } catch {
      case e: ClassNotFoundException =>
        println("Could not load Database driver: " + e)
        exit(-1)
    }

    val retries = 10
    breakable {for (i <- 0 until retries) {
      if (i >= 7) {Thread.sleep(1000)}
      println("Connecting to database...")

      try {
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
    statement.execute(" create table onruntime  (timestamp timestamp , data float (10), PRIMARY KEY (timestamp))")
    statement
  }

  def ExecutePreparedInsertLStatement (preparedStatement: String,connection: Connection, timestamp: Timestamp, value:Float) = {
    val prepared = connection.prepareStatement(preparedStatement)
    prepared.setTimestamp(1, timestamp)
    prepared.setFloat(2, value)
    prepared.executeUpdate()
  }
  def ExecutePreparedSelectStatement (preparedStatement: String,connection: Connection, timestamp: Timestamp) = {
    val prepared = connection.prepareStatement(preparedStatement)
    prepared.setTimestamp(1, timestamp)
    prepared.executeQuery()
  }

}


