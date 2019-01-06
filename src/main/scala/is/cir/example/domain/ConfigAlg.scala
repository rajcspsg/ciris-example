package is.cir.example.domain

import java.net.InetAddress

import cats.MonadError
import cats.syntax.flatMap._
import ciris.cats._
import ciris.enumeratum._
import ciris.kubernetes.SecretKey
import ciris.refined._
import ciris.{ConfigResult, _}
import eu.timepit.refined.auto._
import eu.timepit.refined.types.net.UserPortNumber
import is.cir.example.domain.config.AppEnvironment.{Local, Production, Testing}
import is.cir.example.domain.config.{ApiKey, AppEnvironment, Config}

abstract class ConfigAlg[F[_]](implicit me: MonadError[F, Throwable]) {
  def env[Value](key: String)(
    implicit decoder: ConfigDecoder[String, Value]
  ): ConfigEntry[F, String, String, Value]

  def prop[Value](key: String)(
    implicit decoder: ConfigDecoder[String, Value]
  ): ConfigEntry[F, String, String, Value]

  def secret[Value](name: String)(
    implicit decoder: ConfigDecoder[String, Value]
  ): ConfigEntry[F, SecretKey, String, Value]

  final def loadConfig: F[Config] = {
    val host =
      env[InetAddress]("HOST")
        .orElse(prop("host"))

    val port =
      env[UserPortNumber]("PORT")
        .orElse(prop("http.port"))

    val errorsOrConfig: ConfigResult[F, Config] =
      withValue(env[Option[AppEnvironment]]("APP_ENV")) {
        case Some(Local) | None =>
          ciris.loadConfig(
            host.orNone,
            port.orNone,
            env[Secret[ApiKey]]("API_KEY")
              .orElse(prop("api.key"))
              .orNone
          ) { (host, port, apiKey) =>
            Config.withDefaults(
              environment = Local,
              apiKey = apiKey getOrElse ApiKey.LocalDefault,
              host = host getOrElse InetAddress.getLoopbackAddress,
              port = port getOrElse 9000
            )
          }

        case Some(environment @ (Testing | Production)) =>
          ciris.loadConfig(
            host.orNone,
            port,
            secret[Secret[ApiKey]]("api-key")
          ) { (host, port, apiKey) =>
            Config.withDefaults(
              environment = environment,
              apiKey = apiKey,
              host = host getOrElse InetAddress.getByName("0.0.0.0"),
              port = port
            )
          }
      }

    errorsOrConfig.result flatMap  {
      case Right(config) => me.pure(config)
      case Left(errors)  => me.raiseError(new Exception(errors.toString))
    }
  }
}
