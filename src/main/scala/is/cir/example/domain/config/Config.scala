package is.cir.example.domain.config

import java.net.InetAddress
import cats.Show
import cats.derived._
import cats.implicits._
import ciris.Secret
import ciris.cats._
import enumeratum.EnumEntry
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import is.cir.example.domain.config.AppEnvironment.{Local, Production, Testing}

import scala.concurrent.duration._

final case class Config(
  appName: NonEmptyString,
  environment: AppEnvironment,
  api: ApiConfig
)

object Config {

  import cats.implicits._

  implicit val showConfig: Show[Config] = {
      //implicit val showDuration: Show[Duration] =
     // Show.fromToString

    implicit val showInetAddress: Show[InetAddress] =
      Show.fromToString

    implicit def showEnumEntry[E <: EnumEntry]: Show[E] =
      Show.show(_.entryName)

    semi.show
  }

  def withDefaults(
    environment: AppEnvironment,
    apiKey: Secret[ApiKey],
    host: InetAddress,
    port: UserPortNumber
  ): Config = {
    Config(
      appName = "my-api",
      environment = environment,
      api = ApiConfig(
        host = host,
        port = port,
        apiKey = apiKey,
        timeout = environment match {
          case Local                => Duration.Inf
          case Testing | Production => 10.seconds
        }
      )
    )
  }
}
