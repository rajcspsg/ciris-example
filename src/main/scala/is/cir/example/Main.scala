package is.cir.example

import cats.effect.{ExitCode, IO, IOApp}
import is.cir.example.application.{CirisConfig, ConsoleLogging, Http4sApi}
import is.cir.example.domain.HttpServiceAlg

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    def httpService(): HttpServiceAlg[IO] =
  HttpServiceAlg(CirisConfig, ConsoleLogging, Http4sApi()(ExecutionContext.global))

    httpService().serveHttpApi.unsafeRunSync.compile.drain.map(_ => ExitCode.Success)
  }

}
