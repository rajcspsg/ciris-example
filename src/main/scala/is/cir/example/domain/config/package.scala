package is.cir.example.domain

import ciris.Secret
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.string.MatchesRegex

package object config {
  type ApiKey = String Refined MatchesRegex["[a-zA-Z0-9]{25,40}"]

  object ApiKey {
    val LocalDefault: Secret[ApiKey] =
      Secret("7CpL6XbgnHFp3aKYduyvUpkxAC")
  }
}
