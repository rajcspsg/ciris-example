package is.cir.example.domain

import cats.instances.try_._
import ciris.cats._
import ciris.kubernetes.SecretKey
import ciris.{ConfigDecoder, ConfigEntry, ConfigKeyType, ConfigSource}
import is.cir.example.domain.config.AppEnvironment.{Local, Production, Testing}
import is.cir.example.domain.config.{ApiKey, AppEnvironment, Config}
import utest._

import scala.util.{Failure, Success, Try}

object ConfigAlgTests extends TestSuite {
  override val tests: Tests = Tests {
    'host - {
      def checkHostEnv(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) =
        checkSuccess(
          set(envs = envs ++ Map("HOST" -> "1.2.3.4"), secrets = secrets),
          _.api.host.getHostAddress ==> "1.2.3.4"
        )

      def checkHostProp(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) =
        checkSuccess(
          set(props = Map("host" -> "1.2.3.4"), envs = envs, secrets = secrets),
          _.api.host.getHostAddress ==> "1.2.3.4"
        )

      def checkHostEnvAndProp(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) =
        checkSuccess(
          set(
            envs = envs ++ Map("HOST" -> "1.2.3.4"),
            props = Map("host" -> "invalid"),
            secrets = secrets
          ),
          _.api.host.getHostAddress ==> "1.2.3.4"
        )

      def checkHostInvalid(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) = {
        checkFailure(set(envs = envs ++ Map("HOST" -> "invalid"), secrets = secrets))
        checkFailure(set(props = Map("host" -> "invalid"), envs = envs, secrets = secrets))
      }

      'local - {
        'hostEnv - checkHostEnv()
        'hostProp - checkHostProp()
        'hostEnvAndProp - checkHostEnvAndProp()
        'hostInvalid - checkHostInvalid()
      }

      'testing - {
        val envs = Map("APP_ENV" -> "Testing", "PORT" -> "4000")
        val secrets = Map("api-key" -> apiKey)

        'hostEnv - checkHostEnv(envs, secrets)
        'hostProp - checkHostProp(envs, secrets)
        'hostEnvAndProp - checkHostEnvAndProp(envs, secrets)
        'hostInvalid - checkHostInvalid(envs, secrets)
      }

      'production - {
        val envs = Map("APP_ENV" -> "Production", "PORT" -> "4000")
        val secrets = Map("api-key" -> apiKey)

        'hostEnv - checkHostEnv(envs, secrets)
        'hostProp - checkHostProp(envs, secrets)
        'hostEnvAndProp - checkHostEnvAndProp(envs, secrets)
        'hostInvalid - checkHostInvalid(envs, secrets)
      }
    }

    'port - {
      def checkPortEnv(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) =
        checkSuccess(
          set(envs = envs ++ Map("PORT" -> "4000"), secrets = secrets),
          _.api.port.value ==> 4000
        )

      def checkPortProp(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) =
        checkSuccess(
          set(props = Map("http.port" -> "4000"), envs = envs, secrets = secrets),
          _.api.port.value ==> 4000
        )

      def checkPortEnvAndProp(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) =
        checkSuccess(
          set(
            envs = envs ++ Map("PORT" -> "4000"),
            props = Map("http.port" -> "1023"),
            secrets = secrets
          ),
          _.api.port.value ==> 4000
        )

      def checkPortInvalid(
        envs: Map[String, String] = Map.empty,
        secrets: Map[String, String] = Map.empty
      ) = {
        checkFailure(set(envs = envs ++ Map("PORT" -> "1023"), secrets = secrets))
        checkFailure(set(props = Map("http.port" -> "1023"), envs = envs, secrets = secrets))
        checkFailure(set(envs = envs ++ Map("PORT" -> "49152"), secrets = secrets))
        checkFailure(set(props = Map("http.port" -> "49152"), envs = envs, secrets = secrets))
        checkFailure(set(envs = envs ++ Map("PORT" -> "invalid"), secrets = secrets))
        checkFailure(set(props = Map("http.port" -> "invalid"), envs = envs, secrets = secrets))
      }

      'local - {
        'portEnv - checkPortEnv()
        'portProp - checkPortProp()
        'portEnvAndProp - checkPortEnvAndProp()
        'portInvalid - checkPortInvalid()
      }

      'testing - {
        val envs = Map("APP_ENV" -> "Testing")
        val secrets = Map("api-key" -> apiKey)

        'portEnv - checkPortEnv(envs, secrets)
        'portProp - checkPortProp(envs, secrets)
        'portEnvAndProp - checkPortEnvAndProp(envs, secrets)
        'portInvalid - checkPortInvalid(envs, secrets)
      }

      'production - {
        val envs = Map("APP_ENV" -> "Production")
        val secrets = Map("api-key" -> apiKey)

        'portEnv - checkPortEnv(envs, secrets)
        'portProp - checkPortProp(envs, secrets)
        'portEnvAndProp - checkPortEnvAndProp(envs, secrets)
        'portInvalid - checkPortInvalid(envs, secrets)
      }
    }

    'env - {
      def checkLowerCaseEnv(env: AppEnvironment) =
        checkFailure(set(envs = Map("APP_ENV" -> env.entryName.toLowerCase)))

      def checkUppercaseEnv(env: AppEnvironment) =
        checkFailure(set(envs = Map("APP_ENV" -> env.entryName.toUpperCase)))

      'local - {
        'lowerCaseEnv - checkLowerCaseEnv(Local)
        'upperCaseEnv - checkUppercaseEnv(Local)
      }

      'testing - {
        'lowerCaseEnv - checkLowerCaseEnv(Testing)
        'upperCaseEnv - checkUppercaseEnv(Testing)
      }

      'production - {
        'lowerCaseEnv - checkLowerCaseEnv(Production)
        'upperCaseEnv - checkUppercaseEnv(Production)
      }
    }

    'local - {
      def checkDefaults(config: Config): Unit =
        assert(
          config.environment == AppEnvironment.Local,
          config.api.apiKey == ApiKey.LocalDefault,
          config.api.host.getHostAddress == "127.0.0.1",
          config.api.port.value == 9000,
          !config.api.timeout.isFinite
        )

      'defaults - checkSuccess(set(), checkDefaults)

      'defaultsEnv - checkSuccess(
        set(envs = Map("APP_ENV" -> "Local")),
        checkDefaults
      )

      'apiKeyEnv - checkSuccess(
        set(envs = Map("API_KEY" -> apiKey)),
        _.api.apiKey.value.value ==> apiKey
      )

      'apiKeyProp - checkSuccess(
        set(props = Map("api.key" -> apiKey)),
        _.api.apiKey.value.value ==> apiKey
      )

      'apiKeyEnvAndProp - checkSuccess(
        set(
          envs = Map("API_KEY" -> apiKey),
          props = Map("api.key" -> "changeme")
        ),
        _.api.apiKey.value.value ==> apiKey
      )

      'apiKeyInvalid - {
        checkFailure(set(envs = Map("API_KEY" -> "changeme")))
        checkFailure(set(props = Map("api.key" -> "changeme")))
      }
    }

    'testingOrProduction - {
      def checkDefaults(env: AppEnvironment) =
        checkSuccess(
          set(
            envs = Map("APP_ENV" -> env.entryName, "PORT" -> "4000"),
            secrets = Map("api-key" -> apiKey)
          ),
          config => {
            assert(
              config.environment == env,
              config.api.host.getHostAddress == "0.0.0.0",
              config.api.port.value == 4000,
              config.api.apiKey.value.value == apiKey,
              config.api.timeout.toSeconds == 10L
            )
          }
        )

      'defaultsWithPortAndApiKeyTesting - checkDefaults(Testing)

      'defaultsWithPortAndApiKeyProduction - checkDefaults(Production)

      'failWithoutPortAndApiKey - {
        checkFailure(set(envs = Map("APP_ENV" -> "Testing")))
        checkFailure(set(envs = Map("APP_ENV" -> "Production")))
      }

      'failWithoutPort - {
        checkFailure(set(envs = Map("APP_ENV" -> "Testing"), secrets = Map("api-key" -> apiKey)))
        checkFailure(set(envs = Map("APP_ENV" -> "Production"), secrets = Map("api-key" -> apiKey)))
      }

      'failWithoutApiKey - {
        checkFailure(set(envs = Map("APP_ENV" -> "Testing", "PORT" -> "4000")))
        checkFailure(set(envs = Map("APP_ENV" -> "Testing"), props = Map("http.port" -> "4000")))
        checkFailure(set(envs = Map("APP_ENV" -> "Production", "PORT" -> "4000")))
        checkFailure(set(envs = Map("APP_ENV" -> "Production"), props = Map("http.port" -> "4000")))
      }

      'failWithApiKeyEnvAndProp - {
        checkFailure {
          set(
            envs = Map("APP_ENV" -> "Testing", "API_KEY" -> apiKey),
            props = Map("http.port" -> "4000", "api.key" -> apiKey)
          )
        }

        checkFailure {
          set(
            envs = Map("APP_ENV" -> "Production", "API_KEY" -> apiKey),
            props = Map("http.port" -> "4000", "api.key" -> apiKey)
          )
        }
      }
    }

    'errors - {
      def envs(env: AppEnvironment): String => Try[Option[String]] =
        key => {
          if (key == "APP_ENV")
            Success(Some(env.entryName))
          else fail(key)
        }

      'local - {
        'failOnEnvErrors - checkFailure(alg(envs = fail))
        'failOnPropErrors - checkFailure(alg(props = fail))
        'notFailOnSecretErrors - checkSuccess(alg(secrets = fail))
      }

      'testing - {
        'failOnEnvErrors - checkFailure(alg(envs = envs(Testing)))
        'failOnPropErrors - checkFailure(alg(envs = envs(Testing), props = fail))
        'failOnSecretErrors - checkFailure(alg(envs = envs(Testing), secrets = fail))
      }

      'production - {
        'failOnEnvErrors - checkFailure(alg(envs = envs(Production)))
        'failOnPropErrors - checkFailure(alg(envs = envs(Production), props = fail))
        'failOnSecretErrors - checkFailure(alg(envs = envs(Production), secrets = fail))
      }
    }
  }

  val apiKey: String = "T9fj84BgmrrmYrZJeYpACpLMN"

  def fail[A]: A => Try[Option[String]] = _ => Failure(new Error("error"))

  def missing[A]: A => Try[Option[String]] = _ => Success(None)

  def alg(
    envs: String => Try[Option[String]] = missing,
    props: String => Try[Option[String]] = missing,
    secrets: SecretKey => Try[Option[String]] = missing
  ): ConfigAlg[Try] = new ConfigAlg[Try] {
    override def env[Value](key: String)(
      implicit decoder: ConfigDecoder[String, Value]
    ): ConfigEntry[Try, String, String, Value] =
      ConfigSource
        .fromOptionF(ConfigKeyType.Environment)(envs)
        .read(key)
        .decodeValue[Value]

    override def prop[Value](key: String)(
      implicit decoder: ConfigDecoder[String, Value]
    ): ConfigEntry[Try, String, String, Value] =
      ConfigSource
        .fromOptionF(ConfigKeyType.Property)(props)
        .read(key)
        .decodeValue[Value]

    override def secret[Value](name: String)(
      implicit decoder: ConfigDecoder[String, Value]
    ): ConfigEntry[Try, SecretKey, String, Value] =
      ConfigSource
        .fromOptionF(ConfigKeyType[SecretKey]("kubernetes secret"))(secrets)
        .read(secretKey(name))
        .decodeValue[Value]
  }

  def set(
    envs: Map[String, String] = Map.empty,
    props: Map[String, String] = Map.empty,
    secrets: Map[String, String] = Map.empty
  ): ConfigAlg[Try] = {
    val secretsBySecretKeys =
      secrets.map {
        case (key, value) =>
          secretKey(key) -> value
      }

    alg(
      envs = key => Success(envs.get(key)),
      props = key => Success(props.get(key)),
      secrets = key => Success(secretsBySecretKeys.get(key))
    )
  }

  def secretKey(name: String): SecretKey =
    SecretKey(namespace = "secrets", name = name, key = None)

  def checkFailure(alg: ConfigAlg[Try]): Throwable = {
    val configTry = alg.loadConfig
    assert(configTry.isFailure)
    configTry.failed.get
  }

  def checkSuccess(alg: ConfigAlg[Try], asserts: Config => Unit = _ => ()): Config = {
    val configTry = alg.loadConfig
    assert(configTry.isSuccess)
    val config = configTry.get
    asserts(config)
    config
  }
}
