package com.example.demo.config;

import com.example.demo.client.jph.JsonPlaceholderClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "jph",
        types = {JsonPlaceholderClient.class})
@ImportHttpServices(group = "github", basePackages = "com.example.demo.client.github")
@Import(MyHttpServiceRegistrar.class)
public class HttpClientConfig {
    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> {
            groups.filterByName("jph")
                    .forEachClient(
                            (group, clientBuilder) -> {
                                clientBuilder.defaultStatusHandler(new CustomErrorHandler());
                            });

            groups.filterByName("ara")
                    .forEachClient(
                            (group, clientBuilder) -> {
                                clientBuilder.requestInterceptor(new LoggingInterceptor());
                            });

            groups.filterByName("github")
                    .forEachClient(
                            (group, builder) ->
                                    builder.baseUrl("https://api.github.com")
                                            .defaultHeader(
                                                    "Accept", "application/vnd.github.v3+json"));
        };
    }

    @Bean
    OAuth2RestClientHttpServiceGroupConfigurer securityConfigurer(
            OAuth2AuthorizedClientManager manager) {
        return OAuth2RestClientHttpServiceGroupConfigurer.from(manager);
    }

    /**
     * Custom argument resolver configuration for HttpServiceProxyFactory.
     *
     * <p>NOTE: The API for registering custom argument resolvers with @ImportHttpServices in Spring
     * Boot 4.0 is still evolving. The RequestIdArgumentResolver and @RequestId annotation have been
     * implemented to demonstrate the concept.
     *
     * <p>When the API stabilizes, you would register the resolver like this:
     *
     * <pre>
     * @Bean
     * Consumer<HttpServiceProxyFactory.Builder> httpServiceProxyFactoryCustomizer() {
     *     return builder -> builder.customArgumentResolvers(
     *         List.of(new RequestIdArgumentResolver())
     *     );
     * }
     * </pre>
     *
     * <p>See docs/CUSTOM_ARGUMENT_RESOLVER.md for full documentation.
     */

    // @Bean
    OAuth2RestClientHttpServiceGroupConfigurer securityConfigurer(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        var authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        var authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return OAuth2RestClientHttpServiceGroupConfigurer.from(authorizedClientManager);
    }
}
