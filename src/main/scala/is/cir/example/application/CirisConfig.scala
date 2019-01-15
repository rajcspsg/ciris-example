package is.cir.example.application

import cats.Eval
import cats.effect.IO
import ciris._
import ciris.cats._
import ciris.cats.effect._
import ciris.enumeratum._
import ciris.kubernetes._
import ciris.refined._
import eu.timepit.refined.auto._
import io.kubernetes.client.util
import is.cir.example.domain.ConfigAlg

sealed abstract case class CirisConfig(secretF: SecretInNamespace[IO]) extends ConfigAlg[IO] {
  override def env[Value](key: String)(
    implicit decoder: ConfigDecoder[String, Value]
  ): ConfigEntry[IO, String, String, Value] = {
    envF[IO, Value](key)
  }

  override def prop[Value](key: String)(
    implicit decoder: ConfigDecoder[String, Value]
  ): ConfigEntry[IO, String, String, Value] = {
    propF[IO, Value](key)
  }

  override def secret[Value](name: String)(
    implicit decoder: ConfigDecoder[String, Value]
  ): ConfigEntry[IO, SecretKey, String, Value] = {
    secretF(name)
  }
}

object CirisConfig {
  def apply(): IO[CirisConfig] =
    IO(util.Config.defaultClient)
      .map(secretInNamespace[IO]("secrets", _))
      .map(new CirisConfig(_) {})
}
