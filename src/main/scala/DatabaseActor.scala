import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import java.sql._
import scala.sys.exit
import scala.util.control.Breaks.breakable

case class Row(timeStamp:Timestamp, value:Float)

class DatabaseActor extends Actor with ActorLogging {
  val con: java.sql.Connection = connect()
  afterDBStartup()
  val preparedInsert = con.prepareStatement("insert into onruntime values(?,?)")
  val preparedSelect = con.prepareStatement("select data from onruntime where timestamp= ?")
  val cluster= Cluster(context.system)

  override def preStart(): Unit ={cluster.subscribe(self, classOf[MemberUp])}

  def receive() = {

    case dataPackage:List[Row] =>
      dataPackage.foreach(x =>
      try {
        ExecutePreparedInsertLStatement(x.timeStamp, x.value)
      }catch{
        case e: Exception => println("Error: " + e)
      })

    case "stop" =>
      con.close()
      println("Actor1 stopped.")
      context.stop(self)

    case timestampRequest: Timestamp =>
      try {
       val result = ExecutePreparedSelectStatement(timestampRequest)
        while(result.next()){
          val resultData = result.getFloat("data")
          sender ! resultData
        }
      }catch{
        case e: Exception => println("ErrorWhileSelect: " + e)
      }
    case _:String => print(_)
    case _:MemberUp =>
  }

  //establish a connection to a local in memory database
  def connect(): Connection = {
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
        println("Successfully connected")
        return DriverManager.getConnection("jdbc:h2:./ressources/database")
      }catch {
        case sqle: SQLException =>
          println("Failed to connect to database attempt " + i)
          println(sqle.getMessage)
        case ie: InterruptedException =>
          println("Thread interrupted? Should not happen. " + ie)
      }
    }}
    null
  }

  def ExecutePreparedInsertLStatement (timestamp: Timestamp, value:Float) = {
    preparedInsert.setTimestamp(1, timestamp)
    preparedInsert.setFloat(2, value)
    preparedInsert.executeUpdate()
  }
  def ExecutePreparedSelectStatement (timestamp: Timestamp) = {
    preparedSelect.setTimestamp(1, timestamp)
    preparedSelect.executeQuery()
  }
  def afterDBStartup () = {
    val statement = con.createStatement()
    statement.execute("drop table if exists onruntime")
    statement.execute(" create table onruntime  (timestamp timestamp , data float (10), PRIMARY KEY (timestamp))")
    statement.close()
  }

}


