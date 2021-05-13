import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {
  private val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

  def parseDateTime(text: String) =
    LocalDateTime.parse(text, format)

  def toDateTime(date: LocalDateTime) =
    date.format(format)

  def createSystem(fileName: String, systemName: String): ActorSystem = {
    val resource = getClass.getResource(fileName)
    val configFile=resource.getFile
    val config = ConfigFactory.parseFile(new File(configFile)).resolve()
    val result = ActorSystem(systemName, config)
    result
  }
}
