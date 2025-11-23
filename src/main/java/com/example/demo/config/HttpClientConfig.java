package com.example.demo.config;

import com.example.demo.client.jph.JsonPlaceholderClient;

import org.springframework.beans.factory.annotation.Value;
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
@ImportHttpServices(group = "httpbin", basePackages = "com.example.demo.client.httpbin")
@Import(MyHttpServiceRegistrar.class)
public class HttpClientConfig {

    @Value("${httpbin.auth.username}")
    private String httpbinUsername;

    @Value("${httpbin.auth.password}")
    private String httpbinPassword;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> {
            groups.filterByName("jph")
                    // Allow further filtering of client withing the group
                    .filter(
                            httpServiceGroup ->
                                    httpServiceGroup
                                            .httpServiceTypes()
                                            .contains(JsonPlaceholderClient.class))
                    .forEachClient(
                            (group, clientBuilder) -> {
                                clientBuilder.defaultStatusHandler(new CustomErrorHandler());

                                // custom restclient builder
                                // clientBuilder.apply((builder) -> {});
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

            groups.filterByName("httpbin")
                    .forEachClient(
                            (group, builder) ->
                                    builder.apply(
                                            restClientBuilder ->
                                                    restClientBuilder.defaultHeaders(
                                                            headers ->
                                                                    headers.setBasicAuth(
                                                                            httpbinUsername,
                                                                            httpbinPassword))));
        };
    }

    @Bean
    OAuth2RestClientHttpServiceGroupConfigurer securityConfigurer(
            OAuth2AuthorizedClientManager manager) {
        return OAuth2RestClientHttpServiceGroupConfigurer.from(manager);
    }

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
