# Melior Service Harness :: Web : REST
<div style="display: inline-block;">
<img src="https://img.shields.io/badge/version-2.2-green?style=for-the-badge"/>
<img src="https://img.shields.io/badge/production-ready-green?style=for-the-badge"/>
<img src="https://img.shields.io/badge/compatibility-spring_boot_2.4.5-green?style=for-the-badge"/>
</div>
<div style="display: inline-block;">
<img src="https://img.shields.io/badge/version-2.3-green?style=for-the-badge"/>
<img src="https://img.shields.io/badge/production-ready-green?style=for-the-badge"/>
<img src="https://img.shields.io/badge/compatibility-spring_boot_2.4.5-green?style=for-the-badge"/>
</div>

## Artefact
Get the artefact and the POM file in the *artefact* folder.
```
<dependency>
    <groupId>org.melior</groupId>
    <artifactId>melior-harness-web-rest</artifactId>
    <version>2.3</version>
</dependency>
```

## Client
Create a bean to instantiate the REST client.  The REST client uses connection pooling to improve performance.
```
@Bean("myclient")
@ConfigurationProperties("myclient")
public RestClient client() {
    return RestClientBuilder.create().build();
}
```

The REST client is auto-configured from the application properties.
```
myclient.url=http://some.service:8000/some/endpoint
myclient.username=user
myclient.password=password
myclient.request-timeout=30
myclient.inactivity-timeout=15
```

Wire in and use the REST client.  There is no need to validate the HTTP response being returned from the target endpoint.  The REST client does so automatically and raises a **RemotingException** if the HTTP response indicates a failure.
```
@Autowired
@Qualifier("myclient")
private RestClient client;

public Response foo(Request request) throws RemotingException {
    return client.post("/path_to_add_to_base_url", request, Response.class);
}
```

The REST client understands the default JSON error response used by Spring Boot, but also the friendlier JSON error response that is generated by the REST service harness (see the next section).
```
{
  "type": "NO_DATA",
  "code": "1403",
  "message": "ORA-01403 no data found"
}
```

If the REST client is used in an application that is built using the REST service harness, then the following HTTP headers are ingested by the REST service harness and forwarded by the REST client to the target endpoint.
```
X-Origin-Id
X-Request-Id
```

If the target endpoint uses a custom JSON error response, implement a **ResponseExceptionMapper** to extract the failure details.  The REST client will then automatically raise a **RemotingException** if the response indicates a failure.
```
public class Response implements ResponseExceptionMapper {
    private String status;
    private String error;

    public ExceptionType getExceptionType() {
        // return null if the response is not a failure
        return (status.startsWith("E")) ? ExceptionType.REMOTING_APPLICATION : null;
    }

    public String getExceptionCode() {
        // return null if the response is not a failure
        return (status.startsWith("E")) ? status : null;
    }

    public String getExceptionMessage() {
        // return null if the response is not a failure
        return (status.startsWith("E")) ? error : null;
    }
}
```

The REST client may be configured using these application properties.

|Name|Default|Description|
|:--------------------|:---|:---|
|`url`||The URL of the target endpoint|
|`username`||The user name, if the target endpoint requires one|
|`password`||The password, if the target endpoint requires one|
|`proxy-url`||The URL of the proxy server|
|`proxy-username`||The proxy user name, if the proxy server requires one|
|`proxy-password`||The proxy password, if the proxy server requires one|
|`key-store`||The path to the key store|
|`key-store-type`|jks|The type of the key store|
|`key-store-password`||The password which is required to access the key store|
|`key-password`||The password which is required to access the key pair in the key store.  A password should be set on the key pair and should at the very least be the same as the password to the key store|
|`trust-store`||The path to the trust store|
|`trust-store-type`|jks|The type of the trust store|
|`trust-store-password`||The password which is required to access the trust store|
|`maximum-connections`|1000|The maximum number of connections to open to the target endpoint|
|`connection-timeout`|30 s|The amount of time to allow for a new connection to open to the target endpoint|
|`request-timeout`|60 s|The amount of time to allow for a request to the target endpoint to complete|
|`inactivity-timeout`|300 s|The amount of time to allow before surplus connections to the target endpoint are pruned|
|`prune-interval`|5 s|The interval at which surplus connections to the target endpoint are pruned|

&nbsp;
## Service
Use the REST service harness to get all the required decoration for a Spring Boot application and a REST controller, along with the standard Melior logging system and a configuration object that may be used to access the application properties anywhere and at any time in the application code, even in the constructor.
```
public class MyApplication extends RestService

public MyApplication(ServiceContext serviceContext) throws ApplicationException {
    super(serviceContext);
}
```

The REST service harness is auto-configured from the application properties.
```
server.port=8000
service.name=myapplication
logging.file.path=/log/file/path
logging.file.history-path=/archive/file/path/%d
logging.level=DEBUG
```

Implement a **configure** method to have more control over accessing the application properties than using @Value annotations or using constructor injection.
```
protected void configure() throws ApplicationException {
    name = configuration.getProperty("service.name");
    active = configuration.getProperty("scanning.active", boolean.class);
}
```

When a failure occurs, raise a **RestInterfaceException** to return a friendly JSON error response that the REST client prefers.  The REST service harness automatically generates the relevant HTTP response.
```
@PostMapping(path = "/some/endpoint", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
@ResponseBody
public Response foo(
    @RequestBody
    final Request request) throws RestInterfaceException
```

The REST service harness automatically ingests the following HTTP headers and makes their values available in the transaction context.  The REST client uses the values from the transaction context to forward these HTTP headers to the target endpoint.  The value of the **X-Request-Id** HTTP header is used as the correlation id in the logs written by the Melior logging system.
```
X-Origin-Id
X-Request-Id
```

## References
Refer to the [**Melior Service Harness :: Core**](https://github.com/MeliorArtefacts/service-harness-core) module for detail on the Melior logging system and available utilities.
