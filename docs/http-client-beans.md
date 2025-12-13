# Spring Boot HTTP Client Configuration Beans

## Overview

Spring Boot 4.0 provides several beans for HTTP client configuration. This document explains the key differences between them.

## HttpClientSettings vs HttpClientsProperties

| Aspect | `HttpClientSettings` | `HttpClientsProperties` |
|--------|---------------------|------------------------|
| **Purpose** | Runtime settings object (resolved/effective values) | Configuration properties class (maps to `application.properties`) |
| **Binding** | Created by Spring Boot at runtime | Bound to `spring.http.clients.*` properties |
| **Type** | Immutable record | `@ConfigurationProperties` bean |
| **SSL** | `sslBundle` (resolved SSL bundle object) | `ssl.bundle` (String name of the bundle) |

### HttpClientsProperties (Configuration Source)

This is the configuration properties class that maps to `spring.http.clients.*` in your `application.properties` or `application.yml`.

```properties
spring.http.clients.connect-timeout=2s
spring.http.clients.read-timeout=1s
spring.http.clients.ssl.bundle=myBundle
spring.http.clients.redirects=follow
```

Properties:
- `connectTimeout` - Connection timeout duration
- `readTimeout` - Read timeout duration
- `ssl.bundle` - Name of the SSL bundle to use (String)
- `redirects` - Redirect policy

### HttpClientSettings (Runtime Result)

This is the resolved runtime settings object that HTTP clients actually use. It's created by Spring Boot from the configuration properties.

Properties:
- `connectTimeout()` - Resolved connection timeout as `Duration`
- `readTimeout()` - Resolved read timeout as `Duration`
- `sslBundle()` - Resolved `SslBundle` object (not just the name)

## HttpServiceClientProperties

Maps to `spring.http.serviceclient.*` and defines groups of HTTP service clients with their own settings.

```properties
spring.http.serviceclient.httpbin.base-url=https://httpbin.org
spring.http.serviceclient.httpbin.connect-timeout=5s
spring.http.serviceclient.httpbin.read-timeout=10s

spring.http.serviceclient.github.base-url=https://api.github.com
spring.http.serviceclient.github.connect-timeout=3s
```

Each group can have:
- `baseUrl` - Base URL for the service
- `connectTimeout` - Connection timeout for this group
- `readTimeout` - Read timeout for this group
- `ssl.bundle` - SSL bundle name for this group
- `redirects` - Redirect policy for this group

## ImperativeHttpClientsProperties

Maps to `spring.http.clients.imperative.*` and provides additional configuration specific to imperative (blocking) HTTP clients.

## ClientHttpRequestFactoryBuilder vs ClientHttpRequestFactory

| Aspect | `ClientHttpRequestFactoryBuilder` | `ClientHttpRequestFactory` |
|--------|----------------------------------|---------------------------|
| **Type** | `HttpComponentsClientHttpRequestFactoryBuilder` | `HttpComponentsClientHttpRequestFactory` |
| **Role** | Builder (configures how to create factories) | Factory (creates HTTP requests) |
| **Pattern** | Builder pattern - holds configuration | Factory pattern - produces requests |
| **Lifecycle** | Used once to build the factory | Used repeatedly to create requests |
| **Mutability** | Configurable (timeouts, SSL, etc.) | Immutable once created |

### Flow

```
Builder (configuration) → Factory (production) → HTTP Requests
```

### ClientHttpRequestFactoryBuilder

Holds settings like timeouts, SSL bundles, and connection pooling configuration. You configure it, then call `.build()` to get a factory.

```java
ClientHttpRequestFactory factory = clientHttpRequestFactoryBuilder
    .withHttpClientCustomizer(httpClient -> /* customize */)
    .build();
```

### ClientHttpRequestFactory

The actual object that `RestClient` or `RestTemplate` uses to create `ClientHttpRequest` instances for each HTTP call.

```java
RestClient restClient = RestClient.builder()
    .requestFactory(clientHttpRequestFactory)
    .build();
```

Both use **Apache HttpComponents (HttpClient 5)** as the underlying HTTP library (hence the "HttpComponents" prefix).

## @ImportHttpServices and HTTP Service Client Creation

### Overview

The `@ImportHttpServices` annotation creates proxy beans for HTTP service interfaces.

```java
@ImportHttpServices(group = "httpbin", basePackages = "com.example.demo.client.httpbin")
```

This creates a bean like:
```
httpbin#com.example.demo.client.httpbin.HttpBinClient - jdk.proxy2.$Proxy71
```

### How It Works

```
@ImportHttpServices(group="httpbin", basePackages="...")
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  1. Scans package for interfaces with @HttpExchange    │
│     (e.g., HttpBinClient interface)                     │
└─────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  2. Looks up config: spring.http.serviceclient.httpbin │
│     - base-url=http://localhost:1080                    │
│     - connect-timeout=5000                              │
│     - read-timeout=5000                                 │
└─────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  3. Creates group-specific RestClient (see below)       │
└─────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  4. Creates JDK Dynamic Proxy implementing the          │
│     interface, backed by HttpServiceProxyFactory        │
└─────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  5. Registers bean:                                     │
│     name: "httpbin#com.example.demo...HttpBinClient"   │
│     type: jdk.proxy2.$Proxy71                          │
└─────────────────────────────────────────────────────────┘
```

### Step 3: Creating the RestClient (Detailed)

```
┌─────────────────────────────────────────────────────────┐
│  Auto-configured beans:                                 │
│  - RestClient.Builder (with OAuth2, interceptors, etc.) │
│  - ClientHttpRequestFactoryBuilder                      │
└─────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  For group "httpbin":                                   │
│                                                         │
│  1. Get group properties from HttpServiceClientProperties│
│     - base-url, connect-timeout, read-timeout           │
│                                                         │
│  2. Create HttpClientSettings with the group's timeouts │
│     and build the factory:                              │
│                                                         │
│     HttpClientSettings settings = HttpClientSettings    │
│       .defaults()                                       │
│       .withConnectTimeout(Duration.ofMillis(5000))      │
│       .withReadTimeout(Duration.ofMillis(5000));        │
│                                                         │
│     ClientHttpRequestFactory factory =                  │
│       clientHttpRequestFactoryBuilder.build(settings);  │
│                                                         │
│  3. Clone the auto-configured RestClient.Builder        │
│     and customize for this group:                       │
│                                                         │
│     restClientBuilder.clone()                           │
│       .baseUrl("http://localhost:1080")                 │
│       .requestFactory(factory)  // group-specific       │
│       .build();                                         │
└─────────────────────────────────────────────────────────┘
```

### Where the Factory With Timeouts Comes From

```
HttpServiceClientProperties (spring.http.serviceclient.httpbin.*)
        │
        │ connect-timeout=5000
        │ read-timeout=5000
        ▼
HttpClientSettings.defaults()
        │ .withConnectTimeout(Duration.ofMillis(5000))
        │ .withReadTimeout(Duration.ofMillis(5000))
        ▼
ClientHttpRequestFactoryBuilder (auto-configured bean)
        │ .build(settings)
        ▼
ClientHttpRequestFactory (with group-specific timeouts)
```

### HttpClientSettings API

`HttpClientSettings` is a record that holds HTTP client configuration:

```java
public record HttpClientSettings(
    @Nullable HttpRedirects redirects,
    @Nullable Duration connectTimeout,
    @Nullable Duration readTimeout,
    @Nullable SslBundle sslBundle
)
```

Create settings with fluent methods:
```java
// From defaults
HttpClientSettings settings = HttpClientSettings.defaults()
    .withConnectTimeout(Duration.ofSeconds(5))
    .withReadTimeout(Duration.ofSeconds(10));

// Combined timeout method
HttpClientSettings settings = HttpClientSettings.defaults()
    .withTimeouts(Duration.ofSeconds(5), Duration.ofSeconds(10));

// With SSL bundle
HttpClientSettings settings = HttpClientSettings.ofSslBundle(sslBundle)
    .withTimeouts(Duration.ofSeconds(5), Duration.ofSeconds(10));
```

### Why Clone the Builder?

The auto-configured `RestClient.Builder` may have:
- OAuth2 interceptors
- Default headers
- Error handlers
- Message converters

By cloning it (not creating new), each group **inherits** these configurations while adding group-specific settings (baseUrl, timeouts).

### Key Beans Involved

| Bean | Role |
|------|------|
| `RestClient.Builder` | Base builder with OAuth2, interceptors, etc. |
| `ClientHttpRequestFactoryBuilder` | Creates factories with specific settings |
| `HttpServiceClientProperties` | Holds group configurations |
| `HttpServiceProxyFactory` | Creates the JDK proxy from RestClient |

### How the Proxy Works

Your interface:
```java
@HttpExchange
public interface HttpBinClient {
    @GetExchange("/get")
    Map<String, Object> get();

    @PostExchange("/post")
    Map<String, Object> post(@RequestBody String body);
}
```

When you call `httpBinClient.get()`:
1. Proxy intercepts the method call
2. Reads `@GetExchange("/get")` annotation
3. Combines with `base-url` → `http://localhost:1080/get`
4. Uses the configured `RestClient` (with 5s timeouts) to make the request
5. Deserializes response to `Map<String, Object>`

### The Group Connection

| Annotation | Properties | Purpose |
|------------|------------|---------|
| `group = "httpbin"` | `spring.http.serviceclient.httpbin.*` | Links annotation to config |

The `group` name is the key that ties everything together - it's used as the property prefix and in the bean name.

## Programmatic Registration with AbstractHttpServiceRegistrar

### Two Ways to Register HTTP Service Interfaces

| Approach | Class/Annotation | Example |
|----------|-----------------|---------|
| **Declarative** | `@ImportHttpServices` | `@ImportHttpServices(group = "jph", types = {JsonPlaceholderClient.class})` |
| **Programmatic** | `AbstractHttpServiceRegistrar` | `registry.forGroup("ara").detectInBasePackages(...)` |

### How AbstractHttpServiceRegistrar Works

```java
public class MyHttpServiceRegistrar extends AbstractHttpServiceRegistrar {

    @Override
    protected void registerHttpServices(GroupRegistry registry, AnnotationMetadata metadata) {
        // Option 1: Register specific classes
        // registry.forGroup("echo").register(EchoServiceA.class, EchoServiceB.class);

        // Option 2: Scan package for @HttpExchange interfaces
        registry.forGroup("ara").detectInBasePackages(RestfulApiClient.class);
    }
}
```

Import it in your configuration:
```java
@Configuration
@Import(MyHttpServiceRegistrar.class)
public class HttpClientConfig {
    // ...
}
```

### Flow

```
@Import(MyHttpServiceRegistrar.class)
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│  Spring calls registerHttpServices() during startup     │
└─────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│  registry.forGroup("ara")                               │
│    .detectInBasePackages(RestfulApiClient.class)        │
│                                                         │
│  - Scans the package where RestfulApiClient is located  │
│  - Finds all @HttpExchange interfaces                   │
│  - Registers them under group "ara"                     │
└─────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│  Links to: spring.http.serviceclient.ara.*              │
│  Creates proxy beans for each interface found           │
└─────────────────────────────────────────────────────────┘
```

### GroupRegistry Methods

| Method | Purpose |
|--------|---------|
| `forGroup("name")` | Start registration for a group |
| `.register(Class<?>...)` | Register specific interface classes |
| `.detectInBasePackages(Class<?>)` | Scan package of the given class for `@HttpExchange` interfaces |

### When to Use Each Approach

| Use Case | Approach |
|----------|----------|
| Simple, static registration | `@ImportHttpServices` |
| Conditional registration | `AbstractHttpServiceRegistrar` |
| Dynamic package scanning | `AbstractHttpServiceRegistrar` |
| Need access to `AnnotationMetadata` | `AbstractHttpServiceRegistrar` |

### Both Produce the Same Result

Whether you use:
```java
@ImportHttpServices(group = "ara", basePackages = "com.example.demo.client.ara")
```

Or:
```java
registry.forGroup("ara").detectInBasePackages(RestfulApiClient.class);
```

Both create proxy beans linked to `spring.http.serviceclient.ara.*` properties.

## Duration Format

Timeout values use ISO 8601 duration format:
- `PT2S` = 2 seconds
- `PT30S` = 30 seconds
- `PT5M` = 5 minutes
- `PT1H` = 1 hour
- `PT1H30M` = 1 hour 30 minutes
