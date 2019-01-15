package is.cir.example.domain

import cats.effect.ExitCode
import fs2.Stream
import is.cir.example.domain.config.ApiConfig

trait HttpApiAlg[F[_]] {
  def serveHttpApi(
    config: ApiConfig,
    logger: LoggingAlg[F]
  ): F[Stream[F, ExitCode]]
}
