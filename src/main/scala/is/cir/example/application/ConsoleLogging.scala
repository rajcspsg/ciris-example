package is.cir.example.application

import cats.effect.IO
import is.cir.example.domain.LoggingAlg

object ConsoleLogging extends LoggingAlg[IO] {
  override def info(message: => String): IO[Unit] =
    IO(println(message))
}
