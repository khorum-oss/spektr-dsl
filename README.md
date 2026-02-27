<span style="display: flex; align-items: center; justify-content: center;">
    <img src="assets/spektr-logo-1.png" alt="alt text" width="120" />
    <h1 style="padding: 0; margin: 0; font-size: 76px">Spektr DSL</h1>
</span>

A Kotlin DSL library for defining REST and SOAP endpoints. Used by the [Spektr](https://github.com/khorum-oss/spektr) server to load endpoint configurations from external JARs at runtime.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.khorum.oss.spektr:spektr-dsl:1.0.0")
}
```

## Usage

Implement the `EndpointModule` interface to define your endpoints:

```kotlin
class MyEndpoints : EndpointModule {
    override fun RestEndpointRegistry.configure() {
        get("/api/hello/{name}") { request ->
            val name = request.pathVariables["name"]
            returnBody(mapOf("message" to "Hello, $name!"))
        }

        post("/api/users") { request ->
            DynamicResponse(status = 201, body = mapOf("created" to true))
        }

        delete("/api/users/{id}") { request ->
            returnStatus(204)
        }
    }

    override fun SoapEndpointRegistry.configureSoap() {
        operation("/ws/greeting", "SayHello") { request ->
            SoapResponse(body = soapEnvelope {
                body {
                    element("SayHelloResponse") {
                        element("message") { content = "Hello from SOAP!" }
                    }
                }
            }.toPrettyString())
        }
    }
}
```

Register it via ServiceLoader in `META-INF/services/org.khorum.oss.spektr.dsl.EndpointModule`:
```
com.example.endpoints.MyEndpoints
```

## REST DSL

### HTTP Methods

```kotlin
get("/path") { request -> returnBody(data) }
post("/path") { request -> returnBody(data) }
put("/path") { request -> returnBody(data) }
patch("/path") { request -> returnBody(data) }
delete("/path") { request -> returnStatus(204) }
options("/path") { request -> returnBody(data) }
```

### Request Properties

```kotlin
request.pathVariables   // Map<String, String> - path parameters
request.queryParams     // Map<String, List<String>> - query string
request.headers         // Map<String, List<String>> - HTTP headers
request.body            // String? - request body
```

### Response Helpers

```kotlin
// Return JSON body with 200 status
returnBody(mapOf("key" to "value"))

// Return specific status code (no body)
returnStatus(204)

// Builder DSL for full control
returnResponse {
    status = 201
    header("Location", "/api/users/123")
    body = mapOf("id" to "123")
}
```

### Full Response Control

```kotlin
DynamicResponse(
    status = 201,
    body = mapOf("key" to "value"),
    headers = mapOf("X-Custom" to "value")
)
```

### Error Scenarios

```kotlin
errorOn(
    method = HttpMethod.GET,
    path = "/api/error",
    status = 500,
    body = mapOf("error" to "Something went wrong")
)
```

### Conditional Response Options

```kotlin
returnResponse {
    options {
        badRequest(name == null, mapOf("error" to "name required"))
        notFound(!exists, mapOf("error" to "not found"))
        ok(mapOf("name" to name))
    }
}
```

## SOAP DSL

### Defining Operations

```kotlin
override fun SoapEndpointRegistry.configureSoap() {
    operation("/ws/myservice", "MyAction") { request ->
        SoapResponse(body = "<MyResponse>...</MyResponse>")
    }
}
```

### SOAP Request Properties

```kotlin
request.headers     // Map<String, List<String>> - HTTP headers
request.soapAction  // String - the SOAPAction value
request.body        // String? - raw SOAP XML body
```

### SOAP Response

```kotlin
SoapResponse(
    status = 200,
    body = "<soap:Envelope>...</soap:Envelope>",
    headers = mapOf("X-Custom" to "value")
)
```

### SOAP Envelope Builder

Build SOAP XML responses programmatically instead of using raw strings:

```kotlin
val envelope = soapEnvelope {
    version = SoapVersion.V1_2
    envelopePrefix = "env"

    namespaces {
        ns("xmlns:ns" to "http://example.com/api")
    }

    header {
        element("ns:token") { content = "abc123" }
    }

    body {
        element("ns:GetUserResponse") {
            element("user") {
                element("name") { content = "John Doe" }
                element("email") { content = "john@example.com" }
            }
        }
    }
}

println(envelope.toPrettyString())
```

The builder supports SOAP 1.1 and 1.2, including fault responses with nested sub-codes.

## License

MIT