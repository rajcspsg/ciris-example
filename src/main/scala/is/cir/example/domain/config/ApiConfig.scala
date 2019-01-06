package is.cir.example.domain.config

import java.net.InetAddress
import cats.Show
import ciris.Secret
import enumeratum.EnumEntry
import eu.timepit.refined.types.net.UserPortNumber
import scala.concurrent.duration._

final case class ApiConfig(
  host: InetAddress,
  port: UserPortNumber,
  apiKey: Secret[ApiKey],
  timeout: Duration
)

object ApiConfig {

  import cats.derived.semi
  import cats.implicits.catsStdShowForDuration
  import eu.timepit.refined.cats._

  implicit val showInetAddress: Show[InetAddress] =
    Show.fromToString

  implicit val showSecret: Show[Secret[ApiKey]] =
    Show.fromToString

  implicit def showEnumEntry[E <: EnumEntry]: Show[E] =
    Show.show(_.entryName)


  implicit val showUserPortNumber: Show[UserPortNumber] =  Show.fromToString

  implicit val showApiConfig: Show[ApiConfig] = semi.show[ApiConfig]
}
