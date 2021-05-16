import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

object Utils {
  def convertStringToTimeStamp(input: String): Timestamp = {
    val date: Date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(input);
    new Timestamp(date.getTime)

  }

  def createSystem(fileName: String, systemName: String): ActorSystem = {
    val resource = getClass.getResource(fileName)
    val configFile=resource.getFile
    val config = ConfigFactory.parseFile(new File(configFile)).resolve()
    val result = ActorSystem(systemName, config)
    result
  }
}
