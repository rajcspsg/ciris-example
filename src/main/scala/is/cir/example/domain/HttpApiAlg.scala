package is.cir.example.domain

import fs2.Stream
import fs2.StreamApp.ExitCode
import is.cir.example.domain.config.ApiConfig

trait HttpApiAlg[F[_]] {
  def serveHttpApi(
    config: ApiConfig,
    logger: LoggingAlg[F]
  ): F[Stream[F, ExitCode]]
}
