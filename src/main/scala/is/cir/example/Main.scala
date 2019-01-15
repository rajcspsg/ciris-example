package is.cir.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.Stream
import is.cir.example.application.{CirisConfig, ConsoleLogging, Http4sApi}
import is.cir.example.domain.HttpServiceAlg

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def httpService: IO[HttpServiceAlg[IO]] =
      CirisConfig().map(HttpServiceAlg(_, ConsoleLogging, Http4sApi()))

    Stream
      .eval(httpService)
      .flatMap(_.serveHttpApi)
      .compile
      .lastOrError
  }
}
