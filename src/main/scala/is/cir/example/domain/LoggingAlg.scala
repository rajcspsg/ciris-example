package is.cir.example.domain

trait LoggingAlg[F[_]] {
  def info(message: => String): F[Unit]
}
