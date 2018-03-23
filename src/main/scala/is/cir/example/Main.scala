package is.cir.example

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import is.cir.example.application.{CirisConfig, ConsoleLogging, Http4sApi}
import is.cir.example.domain.HttpServiceAlg

import scala.concurrent.ExecutionContext

object Main extends StreamApp[IO] {
  override def stream(
    args: List[String],
    requestShutdown: IO[Unit]
  ): Stream[IO, ExitCode] = {
    def httpService(scheduler: Scheduler): HttpServiceAlg[IO] =
      HttpServiceAlg(CirisConfig, ConsoleLogging, Http4sApi()(
        executionContext = ExecutionContext.global,
        scheduler = scheduler))

    Scheduler[IO](corePoolSize = 1)
      .map(httpService)
      .evalMap(_.serveHttpApi)
      .flatMap(identity)
  }
}
