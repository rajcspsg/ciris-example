package is.cir.example.application

import cats.effect._
import cats.implicits._
import org.http4s.dsl.io._
import ciris.Secret
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream}
import is.cir.example.domain.config.{ApiConfig, ApiKey}
import is.cir.example.domain.{HttpApiAlg, LoggingAlg}
import org.http4s.headers.Authorization
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.Timeout
import org.http4s.{AuthScheme, Credentials, HttpService, Request, Response, Status}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}

final case class Http4sApi()(
  implicit scheduler: Scheduler,
  executionContext: ExecutionContext,
) extends HttpApiAlg[IO] {

  implicit val ioConcurrentEffect: Concurrent[IO] = cats.effect.IO.ioConcurrentEffect
  implicit  val ioTimer: Timer[IO] = cats.effect.IO.timer

  def redact(request: Request[IO]): Request[IO] =
    request.transformHeaders(_.redactSensitive())

  def hasApiKey(request: Request[IO], apiKey: Secret[ApiKey]): Boolean =
    request.headers
      .get(Authorization)
      .map(_.credentials)
      .exists {
        case Credentials.Token(AuthScheme.Bearer, apiKey.value.value) => true
        case _                                                        => false
      }

  def withTimeout(timeout: Duration)(service: HttpService[IO]): HttpService[IO] =
    timeout match {
      case finite: FiniteDuration =>  {
        //val customTimeoutRes: Response[IO] = Response[IO](Status.GatewayTimeout)
        //implicit val concurrentIO: Concurrent[IO] = Concurrent.apply(IO.pure())
        Timeout(finite)(service)(ioConcurrentEffect, ioTimer )
       //Timeout(finite, IO.apply(customTimeoutRes))(service)
      }
      case _                      => service
    }

  def httpService(
    apiKey: Secret[ApiKey],
    timeout: Duration,
    logger: LoggingAlg[IO]
  ): HttpService[IO] =
    withTimeout(timeout) {
      HttpService[IO] {
        case request if hasApiKey(request, apiKey) =>
          logger.info(s"Received authorized request: ${redact(request)}") *> NoContent()
        case request =>
          logger.info(s"Received unauthorized request: ${redact(request)}") *> Forbidden()
      }
    }

  override def serveHttpApi(
    config: ApiConfig,
    logger: LoggingAlg[IO]
  ): IO[Stream[IO, ExitCode]] =
    IO {
      BlazeBuilder[IO]
        .bindHttp(config.port.value, config.host.getHostAddress)
        .mountService(httpService(config.apiKey, config.timeout, logger))
        .serve
    }
}
