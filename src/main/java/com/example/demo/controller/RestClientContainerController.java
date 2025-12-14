package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.demo.client.jph.JsonPlaceholderClient;
import com.example.demo.config.restclient.RestClientContainer;
import com.example.demo.model.Post;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Test controller for validating {@link RestClientContainer} configuration. Tests authentication
 * (basic, bearer, oauth2), API versioning, and HTTP service client integration.
 */
@RestController
@RequestMapping("/restclient-container")
public class RestClientContainerController {

    private final RestClientContainer restClients;

    public RestClientContainerController(RestClientContainer restClients) {
        this.restClients = restClients;
    }

    /** Lists all registered RestClient names in the container. */
    @GetMapping("/restclient-names")
    public Map<String, Object> getNames() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("availableClients", restClients.getNames());
        return result;
    }

    /**
     * Tests all configured RestClients (jph, ara, httpbin) with their default paths. Validates
     * authentication configuration: bearer (jph), none (ara), basic (httpbin).
     */
    @GetMapping("/test-all-restclients")
    public Map<String, Object> testAll() {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("availableClients", restClients.getNames());

        // Test JPH (bearer auth)
        results.put("jph", testClient("jph", "/posts/1"));

        // Test ARA (no auth)
        results.put("ara", testClient("ara", "/objects"));

        // Test HTTPBin (basic auth)
        results.put("httpbin", testClient("httpbin", "/get"));

        return results;
    }

    /** Tests a specific RestClient using root path. Uses default API version from properties. */
    @GetMapping("/test-one-restclinet/{name}")
    public Map<String, Object> testClient(@PathVariable String name) {
        return testClient(name, "/");
    }

    /** Tests a specific RestClient with custom path. Uses default API version from properties. */
    @GetMapping("/test-one-restclinet/{name}/{*path}")
    public Map<String, Object> testClientWithPath(
            @PathVariable String name, @PathVariable String path) {
        return testClient(name, path);
    }

    private Map<String, Object> testClient(String name, String path) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("client", name);
        result.put("path", path);

        try {
            if (!restClients.contains(name)) {
                result.put("status", "error");
                result.put("message", "Client not found: " + name);
                return result;
            }

            RestClient client = restClients.get(name);
            String response = client.get().uri(path).retrieve().body(String.class);

            result.put("status", "success");
            result.put("response", truncate(response, 500));
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Tests API versioning with per-request version override using .apiVersion().
     *
     * <p>Demonstrates two approaches: 1. response: Uses pre-built RestClient with configured
     * inserter, sets version per-request 2. response2: Uses RestClient.Builder to override the
     * inserter, then sets version per-request
     */
    @GetMapping("/test-set-api-version/{name}/{version}/{*path}")
    public Map<String, Object> testWithVersion(
            @PathVariable String name, @PathVariable String version, @PathVariable String path) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("client", name);
        result.put("path", path);
        result.put("apiVersion", version);

        try {
            if (!restClients.contains(name)) {
                result.put("status", "error");
                result.put("message", "Client not found: " + name);
                return result;
            }

            // Approach 1: Use pre-built RestClient (has pre-configured ApiVersionInserter)
            // Set version per-request using .apiVersion()
            RestClient client = restClients.get(name);
            String response =
                    client.get().uri(path).apiVersion(version).retrieve().body(String.class);

            // Approach 2: Use RestClient.Builder to override ApiVersionInserter
            // Then set version per-request using .apiVersion()
            RestClient.Builder restClientBuilder =
                    restClients
                            .getBuilder(name)
                            .apiVersionInserter(
                                    ApiVersionInserter.useHeader("override-api-versioning-header"));
            String response2 =
                    restClientBuilder
                            .build()
                            .get()
                            .uri(path)
                            .apiVersion(version)
                            .retrieve()
                            .body(String.class);

            result.put("status", "success");
            result.put("response", truncate(response, 500));
            result.put("response2", truncate(response2, 500));
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Tests RestClient.Builder customization by adding a custom header. Demonstrates using
     * getBuilder() to get a pre-configured builder and extend it.
     */
    @GetMapping("/test-restclient-builder/{name}")
    public Map<String, Object> testBuilder(@PathVariable String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("client", name);
        result.put("method", "getBuilder()");

        try {
            if (!restClients.contains(name)) {
                result.put("status", "error");
                result.put("message", "Client not found: " + name);
                return result;
            }

            // Get pre-configured builder and add custom header
            RestClient.Builder builder = restClients.getBuilder(name);
            RestClient customClient =
                    builder.defaultHeader("X-Custom-Test", "from-builder-endpoint").build();

            String response = customClient.get().uri("/").retrieve().body(String.class);

            result.put("status", "success");
            result.put("customHeader", "X-Custom-Test: from-builder-endpoint");
            result.put("response", truncate(response, 500));
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Tests HTTP service interface (JsonPlaceholderClient) using pre-built RestClient from
     * container. The getPostById method has @GetExchange(version="2.0.0"), so it uses version 2.0.0
     * instead of the default version (1.0) configured in properties.
     */
    @GetMapping("/test-jph-exchange-client-by-restclient/{postId}")
    public Map<String, Object> testJphClientFromRestClient(@PathVariable Long postId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("method", "Using pre-built RestClient from container");
        result.put("postId", postId);
        result.put("expectedVersion", "2.0.0 (from @GetExchange annotation)");

        try {
            RestClient client = restClients.get("jph");
            JsonPlaceholderClient jphClient =
                    HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client))
                            .build()
                            .createClient(JsonPlaceholderClient.class);

            Post post = jphClient.getPostById(postId);
            result.put("status", "success");
            result.put("post", post);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Tests HTTP service interface (JsonPlaceholderClient) using RestClient.Builder from container.
     * The getPostById method has @GetExchange(version="2.0.0"), so it uses version 2.0.0 instead of
     * the default version (1.0) configured in properties.
     */
    @GetMapping("/test-jph-exchange-client-by-restclient-builder/{postId}")
    public Map<String, Object> testJphClientFromBuilder(@PathVariable Long postId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("method", "Using RestClient.Builder from container");
        result.put("postId", postId);
        result.put("expectedVersion", "2.0.0 (from @GetExchange annotation)");

        try {
            RestClient.Builder builder = restClients.getBuilder("jph");
            RestClient client = builder.build();
            JsonPlaceholderClient jphClient =
                    HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client))
                            .build()
                            .createClient(JsonPlaceholderClient.class);

            Post post = jphClient.getPostById(postId);
            result.put("status", "success");
            result.put("post", post);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }
}
