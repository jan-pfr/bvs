import Utils.Datapoint
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import java.sql._
import scala.sys.exit
import scala.util.control.Breaks.breakable

class DatabaseActor extends Actor with ActorLogging {
  val con: java.sql.Connection = connect()
  afterDBStartup()
  val preparedInsert = con.prepareStatement("insert into onruntime values(?,?)")
  val preparedSelect = con.prepareStatement("select data from onruntime where timestamp= ?")
  val preparedSelectCount = con.prepareStatement("select count(timestamp) from onruntime")
  val cluster= Cluster(context.system)

  override def preStart(): Unit ={cluster.subscribe(self, classOf[MemberUp])}
  override def postStop(): Unit = {cluster.unsubscribe(self)}

  def receive() = {
    case "getCount" =>
      try{
        val result = ExecutePreparedSelectCountStatement()
        while(result.next()){
          val resultSet = result.getInt("count(timestamp)")
          sender ! resultSet
        }
      }catch{
        case e: Exception => log.info("ErrorWhileSelectCount: {}", e)
      }

    case values:List[Datapoint] =>
      values.foreach(x =>
      try {
        ExecutePreparedInsertLStatement(x.timeStamp, x.value)
      }catch{
        case e: Exception => log.info("Error occurred: {}", e)
      })

    case "stop" =>
      con.close()
      println("Database shutdown.")
      context.stop(self)

    case timestampRequest: Timestamp =>
      try {
       val result = ExecutePreparedSelectStatement(timestampRequest)
        while(result.next()){
          val resultData = result.getFloat("data")
          sender ! resultData
        }
      }catch{
        case e: Exception => log.info("ErrorWhileSelect: {}", e)
      }
    case message => log.info("An unhandled message received: {}", message)
  }

  //establish a connection to a local in memory database
  def connect(): Connection = {
    try { // Load Database driver
      Class.forName("org.h2.Driver")
    } catch {
      case e: ClassNotFoundException =>
        log.info("Could not load Database driver: {}", e)
        exit(-1)
    }

    val retries = 10
    breakable {for (i <- 0 until retries) {
      if (i >= 7) {Thread.sleep(1000)}
      log.info("Connecting to database...")

      try {
        // Connect to database
        log.info("Successfully connected")
        return DriverManager.getConnection("jdbc:h2:./ressources/database")
      }catch {
        case sqle: SQLException =>
          log.info("Failed to connect to database attempt: {}", i)
          log.info(sqle.getMessage)
        case ie: InterruptedException =>
          log.info("Thread interrupted? Should not happen. {} ", ie)
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
  def ExecutePreparedSelectCountStatement() = {
    preparedSelectCount.executeQuery()
  }
  def afterDBStartup () = {
    val statement = con.createStatement()
    statement.execute("drop table if exists onruntime")
    log.info("Table dropped")
    statement.execute(" create table onruntime  (timestamp timestamp , data float (10), PRIMARY KEY (timestamp))")
    statement.close()
  }

}

object DatabaseActor extends App{
  val system = Utils.createSystem("databaseActor.conf", "HFU")
  system.actorOf(Props[DatabaseActor], name= "DatabaseActor")
}


