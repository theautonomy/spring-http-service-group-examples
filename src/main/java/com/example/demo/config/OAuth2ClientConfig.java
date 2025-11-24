package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

@Configuration
public class OAuth2ClientConfig {

    @Value("${httpbin.auth.username}")
    private String httpbinUsername;

    @Value("${httpbin.auth.password}")
    private String httpbinPassword;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurerForOAuth2(
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return groups -> {
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
            groups.filterByName("otc")
                    .forEachClient(
                            (group, clientBuilder) -> {
                                // Add Spring's OAuth2 interceptor for otc
                                var oauth2Interceptor =
                                        new OAuth2ClientHttpRequestInterceptor(
                                                authorizedClientManager);
                                oauth2Interceptor.setClientRegistrationIdResolver(request -> "otc");
                                clientBuilder.requestInterceptor(oauth2Interceptor);
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
