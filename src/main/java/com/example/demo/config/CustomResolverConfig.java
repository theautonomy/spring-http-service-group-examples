package com.example.demo.config;

import com.example.demo.client.ara.RestfulApiSearchClient;
import com.example.demo.resolver.ObjectSearchArgumentResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuration for HTTP Service Client with custom argument resolver.
 *
 * <p>This demonstrates the manual approach to registering custom argument resolvers, following the
 * Spring Framework documentation pattern.
 *
 * @see <a
 *     href="https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-service-client.custom-resolver">
 *     Spring Framework - Custom Argument Resolver</a>
 */
@Configuration
public class CustomResolverConfig {

    /**
     * Creates a RestfulApiSearchClient with custom ObjectSearchArgumentResolver.
     *
     * <p>This follows the pattern from Spring documentation:
     *
     * <pre>
     * RestClient -> RestClientAdapter -> HttpServiceProxyFactory with custom resolver
     * </pre>
     */
    @Bean
    public RestfulApiSearchClient restfulApiSearchClient() {
        // 1. Create RestClient with base URL
        RestClient restClient =
                RestClient.builder()
                        .baseUrl("https://api.restful-api.dev")
                        .requestInterceptor(new LoggingInterceptor())
                        .build();

        // 2. Create adapter from RestClient
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 3. Build HttpServiceProxyFactory with custom argument resolver
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter)
                        .customArgumentResolver(new ObjectSearchArgumentResolver())
                        .build();

        // 4. Create the client proxy
        return factory.createClient(RestfulApiSearchClient.class);
    }
}
