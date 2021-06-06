import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import java.io.InputStreamReader
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

object Utils {
  def convertStringToTimeStamp(input: String): Timestamp = {
    try {
      val date: Date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(input);
      new Timestamp(date.getTime)
    }catch{
      case exception: Exception =>
        new Timestamp(0)
    }

  }

  def createSystem(fileName: String, systemName: String): ActorSystem = {
    val resource = getClass.getClassLoader.getResourceAsStream(fileName)
    val reader = new InputStreamReader(resource)
    val config = ConfigFactory.parseReader(reader).resolve()
    val result = ActorSystem(systemName, config)
    result
  }

}
