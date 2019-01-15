package is.cir.example.domain

import cats.FlatMap
import cats.effect.ExitCode
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import fs2.Stream

final case class HttpServiceAlg[F[_]: FlatMap](
  config: ConfigAlg[F],
  logger: LoggingAlg[F],
  http: HttpApiAlg[F]
) {
  def serveHttpApi: Stream[F, ExitCode] =
    for {
      config <- Stream.eval(config.loadConfig)
      _ <- Stream.eval(logger.info(s"Running with configuration: ${config.show}"))
      api <- Stream.eval(http.serveHttpApi(config.api, logger))
      _ <- Stream.eval(logger.info(s"Http service is running on port ${config.api.port}"))
      exitCode <- api
    } yield exitCode
}
