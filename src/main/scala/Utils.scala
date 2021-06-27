import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import java.io.InputStreamReader
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

object Utils {

  case class DataPoint(timeStamp:Timestamp, value: Float)
  case class DataPackageList(id:String, List:List[String])
  case class DataPackageDataPoint(id:String, List:List[DataPoint])

  trait FinalCaseClassModel extends DefaultJsonProtocol {
    final case class meanTempSuccess(when : String, what: Float)
    final case class meanTempFailure(when : String)
    final case class errorMessage(message : String)
    final case class count(rows : Int)
  }


  trait JsonSupport extends SprayJsonSupport with FinalCaseClassModel {
    implicit val meanTempWithDateFormat = jsonFormat2(meanTempSuccess.apply)
    implicit val meanTempWithoutDateFormat = jsonFormat1(meanTempFailure.apply)
    implicit val errorMessageFormat = jsonFormat1(errorMessage.apply)
    implicit val countFormat = jsonFormat1(count.apply)
  }

  def convertStringToTimeStamp(input: String) = {
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
