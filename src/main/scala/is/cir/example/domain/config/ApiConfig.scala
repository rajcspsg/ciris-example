package is.cir.example.domain.config

import java.net.InetAddress

import ciris.Secret
import eu.timepit.refined.types.net.UserPortNumber

import scala.concurrent.duration.Duration

final case class ApiConfig(
  host: InetAddress,
  port: UserPortNumber,
  apiKey: Secret[ApiKey],
  timeout: Duration
)
