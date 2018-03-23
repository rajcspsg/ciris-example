package is.cir.example.domain.config

import enumeratum.{Enum, EnumEntry}

sealed abstract class AppEnvironment extends EnumEntry

object AppEnvironment extends Enum[AppEnvironment] {
  case object Local extends AppEnvironment
  case object Testing extends AppEnvironment
  case object Production extends AppEnvironment

  override val values: Vector[AppEnvironment] =
    findValues.toVector
}
