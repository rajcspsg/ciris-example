```tut:invisible
import is.cir.example.domain.config.ApiKey
```

## Ciris Example
This is a [Http4s](https://http4s.org) application to exemplify how [Ciris](https://cir.is) can be used for configuration loading. The configuration loading algebra is defined with a final tagless encoding in [`ConfigAlg`][ConfigAlg], abstracting the effect type and configuration sources, for testing purposes. An implementation with Kubernetes secrets ([ciris-kubernetes][ciris-kubernetes]) and [cats-effect][cats-effect] [`IO`][IO] is in [`CirisConfig`][CirisConfig]. The configuration model is in the [`config`][config-package] package, and tests are in [`ConfigAlgTests`][ConfigAlgTests].

### Running: Locally
To run the application locally, make sure [sbt](https://www.scala-sbt.org) is installed, and then run the following.

```
❯ sbt run
```

The application should start with default values.

```
Running with configuration: Config(appName = my-api, environment = Local, api = ApiConfig(host = localhost/127.0.0.1, port = 9000, apiKey = Secret(***), timeout = Duration.Inf))
Http service is running on port 9000
```

The usage guide contains more information about [logging configurations](https://cir.is/docs/logging) (also see [`Config`][Config]).

The API is setup to return `403 Forbidden` for all requests unless the correct authorization token is supplied.

```
❯ curl -I localhost:9000
HTTP/1.1 403 Forbidden
Date: Sun, 25 Mar 2018 20:00:44 GMT
Content-Length: 0
```

```tut:passthrough
println {
  s"""
    |The default authorization token when running locally is `${ApiKey.LocalDefault.value}` (see [`ApiKey`][ApiKey]).
    |```
    |❯ curl -IH 'Authorization: bearer ${ApiKey.LocalDefault.value}' localhost:9000
    |HTTP/1.1 204 No Content
    |Date: Sun, 25 Mar 2018 20:02:43 GMT
    |```
   """.stripMargin
}
```

Both authorized and unauthorized requests are logged to the console.

```
Received unauthorized request: Request(method=HEAD, uri=/, headers=Headers(Host: localhost:9000, User-Agent: curl/7.54.0, Accept: */*))
Received authorized request: Request(method=HEAD, uri=/, headers=Headers(Host: localhost:9000, User-Agent: curl/7.54.0, Accept: */*, Authorization: <REDACTED>))
```

When running locally, the following configuration overrides are available.

```tut:passthrough
println {
  s"""
    |- The host to bind can be overridden with the `HOST` environment variable,  
    |  or with the `host` system property (defaults to the loopback address).
    |- The port to bind can be overridden with the `PORT` environment variable,  
    |  or with the `http.port` system property (defaults to use port `9000`).
    |- The API key can be overridden with the `API_KEY` environment variable,  
    |  or with the `api.key` system property (defaults to `${ApiKey.LocalDefault.value}`).
   """.stripMargin.trim
}
```

Attempts to use invalid configuration values results in configuration loading errors.

```
❯ PORT=900 API_KEY=changeme sbt run
ciris.ConfigException: configuration loading failed with the following errors.

  - Environment variable [PORT] with value [900] cannot be converted to type [eu.timepit.refined.api.Refined[Int,eu.timepit.refined.numeric.Interval.Closed[Int(1024),Int(49151)]]]: Left predicate of (!(900 < 1024) && !(900 > 49151)) failed: Predicate (900 < 1024) did not fail.
  - Environment variable [API_KEY] with value [changeme] cannot be converted to type [eu.timepit.refined.api.Refined[String,eu.timepit.refined.string.MatchesRegex[java.lang.String("[a-zA-Z0-9]{25,40}")]]]: Predicate failed: "changeme".matches("[a-zA-Z0-9]{25,40}").

  at ciris.ConfigException$.apply(ConfigException.scala:34)
  at ciris.ConfigErrors$.toException$extension(ConfigErrors.scala:106)
  ...
```

### Running: Testing & Production
For the `Testing` and `Production` environments, the API key to use is loaded from a Kubernetes secret. Additionally, the port to bind must be specified with the `PORT` environment variable or the `http.port` system property. The host to bind defaults to `0.0.0.0` unless specified with the `HOST` environment variable or the `host` system property.

To run in a different environment, we can set the `APP_ENV` environment variable.

```
❯ APP_ENV=Testing sbt run
ciris.ConfigException: configuration loading failed with the following errors.

  - Missing environment variable [PORT] and missing system property [http.port].
  - Exception while reading kubernetes secret [namespace = secrets, name = api-key]: io.kubernetes.client.ApiException: Not Found.

  at ciris.ConfigException$.apply(ConfigException.scala:34)
  at ciris.ConfigErrors$.toException$extension(ConfigErrors.scala:106)
  ...
```

We can see that we forgot to specify the port and that the Kubernetes secret doesn't exist.  
Note that the errors you see can be different depending on if you have Kubernetes setup.

### Testing
To run the configuration tests in [`ConfigAlgTests`][ConfigAlgTests], run the following.

```
❯ sbt test
```

[ApiKey]: src/main/scala/is/cir/example/domain/config/package.scala
[cats-effect]: https://typelevel.org/cats-effect/
[ciris-kubernetes]: https://github.com/ovotech/ciris-kubernetes
[CirisConfig]: src/main/scala/is/cir/example/application/CirisConfig.scala
[config-package]: src/main/scala/is/cir/example/domain/config
[Config]: src/main/scala/is/cir/example/domain/config/Config.scala
[ConfigAlg]: src/main/scala/is/cir/example/domain/ConfigAlg.scala
[ConfigAlgTests]: src/test/scala/is/cir/example/domain/ConfigAlgTests.scala
[IO]: https://typelevel.org/cats-effect/datatypes/io.html
