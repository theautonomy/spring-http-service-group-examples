# HttpServiceArgumentResolver - Custom Argument Resolver Example

This document demonstrates how to implement and use `HttpServiceArgumentResolver` following the official Spring Framework documentation pattern.

## Overview

`HttpServiceArgumentResolver` allows you to create custom parameter types in HTTP Service Client interfaces that are automatically converted into HTTP request components (query parameters, headers, etc.).

**Reference:** [Spring Framework Documentation - Custom Resolver](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-service-client.custom-resolver)

## Implementation

### 1. Custom Parameter Type

We created `ObjectSearch` record to encapsulate search criteria:

```java
public record ObjectSearch(String name, String color) {
    public static ObjectSearch of(String name, String color) {
        return new ObjectSearch(name, color);
    }
}
```

**Location:** `src/main/java/com/example/demo/model/ObjectSearch.java`

### 2. Custom Argument Resolver

The resolver converts `ObjectSearch` parameters into query parameters:

```java
public class ObjectSearchArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(
            Object argument,
            MethodParameter parameter,
            HttpRequestValues.Builder requestValues) {

        // Check if this parameter is of type ObjectSearch
        if (!parameter.getParameterType().equals(ObjectSearch.class)) {
            return false; // Not handled by this resolver
        }

        // Cast and extract search parameters
        ObjectSearch search = (ObjectSearch) argument;

        // Add query parameters
        if (search.name() != null && !search.name().isEmpty()) {
            requestValues.addRequestParameter("name", search.name());
        }
        if (search.color() != null && !search.color().isEmpty()) {
            requestValues.addRequestParameter("color", search.color());
        }

        return true; // Argument was handled
    }
}
```

**Location:** `src/main/java/com/example/demo/resolver/ObjectSearchArgumentResolver.java`

**How it works:**
1. Checks if the parameter is of type `ObjectSearch`
2. Returns `false` if not (lets other resolvers handle it)
3. Extracts name and color from the search object
4. Adds them as query parameters to the request
5. Returns `true` to indicate the argument was handled

### 3. HTTP Service Client Interface

```java
public interface RestfulApiSearchClient {

    /**
     * Search objects using custom ObjectSearch parameter.
     * The ObjectSearch parameter will be converted to query parameters.
     */
    @GetExchange("/objects")
    List<ApiObject> searchObjects(ObjectSearch search);
}
```

**Location:** `src/main/java/com/example/demo/client/ara/RestfulApiSearchClient.java`

### 4. Manual Client Configuration

Following the Spring documentation pattern, we manually create the client with the custom resolver:

```java
@Configuration
public class CustomResolverConfig {

    @Bean
    public RestfulApiSearchClient restfulApiSearchClient() {
        // 1. Create RestClient with base URL
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.restful-api.dev")
                .requestInterceptor(new LoggingInterceptor())
                .build();

        // 2. Create adapter from RestClient
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 3. Build HttpServiceProxyFactory with custom argument resolver
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .customArgumentResolver(new ObjectSearchArgumentResolver())
                .build();

        // 4. Create the client proxy
        return factory.createClient(RestfulApiSearchClient.class);
    }
}
```

**Location:** `src/main/java/com/example/demo/config/CustomResolverConfig.java`

**Key Steps:**
1. Create `RestClient` with base URL and interceptors
2. Wrap it in `RestClientAdapter`
3. Build `HttpServiceProxyFactory` with `.customArgumentResolver()`
4. Create the client proxy from the factory

## Usage Example

```java
// In JsonPlaceholderTestRunner.java
ObjectSearch search = ObjectSearch.of("MacBook", "Silver");
List<ApiObject> results = restfulApiSearchClient.searchObjects(search);

// This generates:
// GET https://api.restful-api.dev/objects?name=MacBook&color=Silver
```

## Test Output

When running the application, you'll see:

```
=== Testing Custom Argument Resolver (HttpServiceArgumentResolver) ===

   This demonstrates the HttpServiceArgumentResolver pattern from Spring Framework docs.
   The ObjectSearch parameter is automatically converted to query parameters.

1. Searching objects with custom ObjectSearch parameter:
   ObjectSearch.of(null, null) -> GET /objects

=== HTTP Request ===
Method: GET
URI: https://api.restful-api.dev/objects
Headers: [Content-Length:"0"]
====================

=== HTTP Response ===
Status Code: 200 OK
...

   Found 13 objects
   - Google Pixel 6 Pro (ID: 1)
   - Apple iPhone 12 Mini, 256GB, Blue (ID: 2)
   - Apple iPhone 12 Pro Max (ID: 3)

=== Custom Argument Resolver test completed! ===
```

## Comparison with @ImportHttpServices

| Approach | Registration Method | Use Case |
|----------|-------------------|----------|
| **@ImportHttpServices** | Automatic via annotations | Simple clients, standard parameters |
| **Manual Factory** | `HttpServiceProxyFactory.builderFor()` | Custom resolvers, advanced configuration |

**When to use manual factory:**
- ✅ You need custom argument resolvers
- ✅ You need fine-grained control over client creation
- ✅ You want to reuse complex parameter types

**When to use @ImportHttpServices:**
- ✅ Standard HTTP operations only
- ✅ Simple parameter mapping
- ✅ Quick client setup

## Key Takeaways

1. **Interface Contract**: The resolver implements `HttpServiceArgumentResolver` with a single `resolve()` method

2. **Return Value**:
   - Return `true` if the resolver handles the parameter
   - Return `false` to pass to the next resolver

3. **Request Building**: Use `HttpRequestValues.Builder` to add:
   - `.addRequestParameter(name, value)` - Query parameters
   - `.addHeader(name, value)` - Headers
   - `.setUriVariable(name, value)` - Path variables

4. **Registration**: Use `HttpServiceProxyFactory.builderFor(adapter).customArgumentResolver(resolver)`

## Additional Use Cases

This pattern can be extended for:

| Use Case | Parameter Type | Conversion |
|----------|---------------|------------|
| **Search Criteria** | `ObjectSearch` | Query parameters |
| **Pagination** | `PageRequest` | `?page=1&size=20` |
| **Sorting** | `Sort` | `?sort=name,asc` |
| **Date Range** | `DateRange` | `?from=2024-01-01&to=2024-12-31` |
| **Filters** | `FilterCriteria` | Multiple query params |

## Files Created

| File | Purpose |
|------|---------|
| `model/ObjectSearch.java` | Custom parameter type |
| `resolver/ObjectSearchArgumentResolver.java` | Resolver implementation |
| `client/ara/RestfulApiSearchClient.java` | Client interface using custom param |
| `config/CustomResolverConfig.java` | Manual client factory configuration |
| `docs/CUSTOM_ARGUMENT_RESOLVER.md` | This documentation |

## Conclusion

The `HttpServiceArgumentResolver` pattern provides a clean, type-safe way to handle complex parameter types in declarative HTTP clients. By following the manual factory approach from the Spring documentation, we can easily register custom resolvers and maintain full control over client creation.
