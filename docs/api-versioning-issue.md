# API Versioning for HTTP Service Clients

## Usage Summary

### Case 1: No API Versioning (Most Common)

Most APIs don't require versioning. No configuration needed - just use the service client normally.

```properties
# No apiversion properties needed
spring.http.serviceclient.myapi.base-url=https://api.example.com
```

### Case 2: API Versioning Required

When the API requires versioning, configure in two steps:

**Step 1: Set the insertion method** (how version is added to request)

```properties
# Choose ONE insertion method:
spring.http.serviceclient.jph.apiversion.insert.header=X-API-VERSION
# OR spring.http.serviceclient.jph.apiversion.insert.query-parameter=api-version
# OR spring.http.serviceclient.jph.apiversion.insert.path-segment=0
# OR spring.http.serviceclient.jph.apiversion.insert.media-type-parameter=version
```

**Step 2: Set the version** (choose one approach)

Option A - Configure default version in properties:
```properties
serviceclient.jph.api-version-default=1.0
```

Option B - Set programmatically per request:
```java
RestClient client = restClients.get("jph");
client.get()
    .uri("/posts/1")
    .apiVersion("2.0")  // Set version per request
    .retrieve()
    .body(String.class);
```

Option C - Set on RestClient.Builder:
```java
RestClient.Builder builder = restClients.getBuilder("jph");
RestClient client = builder
    .defaultApiVersion("1.0")  // Set default version
    .apiVersionInserter(ApiVersionInserter.useHeader("X-API-VERSION"))  // Set inserter
    .build();
```

Option D - Use annotation on HTTP interface method:
```java
@GetExchange(url = "/posts/{id}", version = "2.0.0")
Post getPostById(@PathVariable Long id);
```

### Version Precedence (Highest to Lowest)

1. Method annotation `version` attribute (`@GetExchange(version = "2.0.0")`)
2. Per-request `.apiVersion()` call
3. `builder.defaultApiVersion()` / `serviceclient.*.api-version-default` property

Note: `apiVersionInserter()` is a **builder method** that configures HOW the version is inserted (header, query param, etc.), not the version value itself.

---

# Spring Boot 4.0 API Versioning Configuration Issue

## Problem

When configuring API versioning for HTTP service clients using Spring Boot 4.0's built-in properties, the `defaultVersion` property does not bind correctly:

```properties
# This does NOT work - defaultVersion is always null
spring.http.serviceclient.jph.apiversion.default-version=1.0
spring.http.serviceclient.jph.apiversion.insert.header=X-API-VERSION
```

The `insert.*` properties (header, query-parameter, path-segment, media-type-parameter) bind correctly, but `default-version` remains `null`.

## Root Cause

This is **by design**, not a bug. The Spring Boot team intentionally decided to not configure API versioning on the default auto-configured RestClient builder.

### Related GitHub Issues

1. **[Issue #47337](https://github.com/spring-projects/spring-boot/issues/47337)** - "apiversion properties aren't always applied to RestClient"
   - Status: **Closed as "Not Planned"**
   - Reported by: Phillip Webb (September 26, 2025)
   - Closed by: Stéphane Nicoll (October 16, 2025)
   - Resolution: *"We no longer configure API versioning on the default auto-configured builder, see #47398"*

2. **[Issue #47398](https://github.com/spring-projects/spring-boot/issues/47398)** - Discussed unifying HTTP client configuration properties
   - The team decided to rationalize/simplify properties but keep modules separate

## Workaround

Use custom configuration properties to set the default API version.

### 1. Add property to custom ClientAuthProperties

```java
public class ClientAuthProperties {
    private String apiVersionDefault; // Workaround for Spring Boot binding issue

    public String getApiVersionDefault() {
        return apiVersionDefault;
    }

    public void setApiVersionDefault(String apiVersionDefault) {
        this.apiVersionDefault = apiVersionDefault;
    }
    // ... other properties
}
```

### 2. Configure in application.properties

```properties
# Spring Boot built-in properties (insert method works)
spring.http.serviceclient.jph.apiversion.insert.header=X-API-VERSION

# Custom property for default version (workaround)
serviceclient.jph.api-version-default=1.0
```

### 3. Apply in RestClient configuration

```java
private void configureApiVersion(
        RestClient.Builder builder,
        HttpClientProperties clientProps,
        @Nullable ClientAuthProperties authProps) {

    if (clientProps.getApiversion() == null) {
        return;
    }

    var apiversion = clientProps.getApiversion();
    var insert = apiversion.getInsert();

    if (insert == null) {
        return;
    }

    // Get default version from custom properties (workaround)
    String defaultVersion = (authProps != null) ? authProps.getApiVersionDefault() : null;

    if (defaultVersion == null) {
        return;
    }

    // Set the default version
    builder.defaultApiVersion(defaultVersion);

    // Create the appropriate ApiVersionInserter
    ApiVersionInserter inserter = createApiVersionInserter(insert);
    if (inserter != null) {
        builder.apiVersionInserter(inserter);
    }
}
```

## API Version Insertion Methods

Spring Framework 7.0 / Spring Boot 4.0 supports four API version insertion methods:

| Method | Property | Result Example |
|--------|----------|----------------|
| Header | `apiversion.insert.header=X-API-VERSION` | `X-API-VERSION: 1.0` |
| Query Parameter | `apiversion.insert.query-parameter=api-version` | `?api-version=1.0` |
| Path Segment | `apiversion.insert.path-segment=0` | `/v1.0/users` |
| Media Type | `apiversion.insert.media-type-parameter=version` | `Content-Type: application/json;version=1.0` |

## Per-Request Version Override

You can override the default version on a per-request basis:

```java
RestClient client = restClients.get("jph");
String response = client.get()
    .uri("/posts/1")
    .apiVersion("2.0")  // Override default version
    .retrieve()
    .body(String.class);
```

## References

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [API Versioning in Spring](https://spring.io/blog/2025/09/16/api-versioning-in-spring/)
- [Spring Boot Built-in API Versioning - Piotr's TechBlog](https://piotrminkowski.com/2025/12/01/spring-boot-built-in-api-versioning/)
- [ApiVersionInserter JavaDoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/ApiVersionInserter.html)
