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
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
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
    public RestClientHttpServiceGroupConfigurer groupConfigurer(
            OAuth2AuthorizedClientManager authorizedClientManager) {
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

                                clientBuilder.requestInterceptor(new LoggingInterceptor());
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
                            (group, clientBuilder) -> {
                                // Add Spring's OAuth2 interceptor for GitHub
                                var oauth2Interceptor =
                                        new OAuth2ClientHttpRequestInterceptor(
                                                authorizedClientManager);
                                oauth2Interceptor.setClientRegistrationIdResolver(
                                        request -> "github");
                                clientBuilder.requestInterceptor(oauth2Interceptor);
                            });

            groups.filterByName("httpbin")
                    .forEachClient(
                            (group, clientBuilder) -> {
                                // HTTP Basic Authentication
                                clientBuilder.apply(
                                        restClientBuilder ->
                                                restClientBuilder.defaultHeaders(
                                                        headers ->
                                                                headers.setBasicAuth(
                                                                        httpbinUsername,
                                                                        httpbinPassword)));

                                // Custom message converter for text/plain and text/html
                                clientBuilder.configureMessageConverters(
                                        (builder) -> {
                                            builder.addCustomConverter(
                                                    new MyCustomMessageConverter());
                                        });
                            });
        };
    }

    @Bean
    OAuth2RestClientHttpServiceGroupConfigurer securityConfigurer(
            OAuth2AuthorizedClientManager manager) {
        return OAuth2RestClientHttpServiceGroupConfigurer.from(manager);
    }

    @Bean
    OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
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

        return authorizedClientManager;
    }
}
