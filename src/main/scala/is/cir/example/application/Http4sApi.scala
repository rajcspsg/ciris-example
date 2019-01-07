package is.cir.example.application

import cats.data.OptionT
import cats.effect._
import cats.implicits._
import org.http4s.dsl.io._
import ciris.Secret
import fs2.Stream
import is.cir.example.domain.config.{ApiConfig, ApiKey}
import is.cir.example.domain.{HttpApiAlg, LoggingAlg}
import org.http4s.headers.Authorization
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware._
import org.http4s.{AuthScheme, Credentials, HttpApp, HttpRoutes, Request}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

final case class Http4sApi()(implicit executionContext: ExecutionContext) extends HttpApiAlg[IO] {

  private implicit val cs: ContextShift[IO] = IO.contextShift(executionContext)
  private implicit val timer: Timer[IO] = IO.timer(executionContext)
  private implicit val ioConcurrent: Concurrent[IO] = cats.effect.IO.ioConcurrentEffect

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

  def withTimeout(timeout: Duration)(routes: HttpRoutes[IO]): HttpApp[IO] =
    (timeout match {
     case finite: FiniteDuration => (Timeout(finite)(routes)).orNotFound
     case _                     => routes.orNotFound
    })

  def httpService(
    apiKey: Secret[ApiKey],
    timeout: Duration,
    logger: LoggingAlg[IO]
  ): HttpApp[IO] = withTimeout(5.milliseconds) {
    HttpRoutes.of[IO] {
      case request if hasApiKey(request, apiKey) =>
        logger.info(s"Received authorized request: ${redact(request)}") *> Ok("thanks for apikey")
      case request =>
        logger.info(s"Received unauthorized request: ${redact(request)}") *> Forbidden()
    }
  }

  override def serveHttpApi(
    config: ApiConfig,
    logger: LoggingAlg[IO]
  ): IO[Stream[IO, ExitCode]] =
    IO {
      BlazeServerBuilder[IO]
        .bindHttp(config.port.value, config.host.getHostAddress)
        .withHttpApp(httpService(config.apiKey, config.timeout, logger))
        .serve
    }
}
