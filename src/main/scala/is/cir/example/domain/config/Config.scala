package is.cir.example.domain.config

import java.net.InetAddress

import cats.Show
import cats.derived._
import ciris.Secret
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
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

  import cats.implicits.catsStdShowForDuration
  import eu.timepit.refined.cats._
  import ApiConfig.{showApiConfig, showEnumEntry }

  //implicit val showNonEmptyString: Show[NonEmptyString] = implicitly[Show[NonEmptyString]]
  implicit val showNonEmptyString: Show[NonEmptyString] = Show.fromToString
  //implicit val showNonEmptyString: Show[NonEmptyString] = refTypeShow[String, String, Refined](catsStdShowForDuration)

  implicit val showConfig: Show[Config] = semi.show[Config]

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
