package com.example.demo.config.oauth2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

@Configuration
@ConditionalOnOAuth2ClientRegistration
public class OAuth2ClientConfig {

    @Value("${httpbin.auth.username}")
    private String httpbinUsername;

    @Value("${httpbin.auth.password}")
    private String httpbinPassword;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurerForOAuth2(
            OAuth2AuthorizedClientManager authorizedClientManager,
            ClientRegistrationRepository clientRegistrationRepository) {
        return groups -> {
            // Get list of OAuth2 client registration IDs from application.properties
            List<String> oauth2Groups = new ArrayList<>();
            if (clientRegistrationRepository instanceof Iterable) {
                for (ClientRegistration registration :
                        (Iterable<ClientRegistration>) clientRegistrationRepository) {
                    oauth2Groups.add(registration.getRegistrationId());
                }
            }

            // Configure OAuth2 interceptor for each group
            oauth2Groups.forEach(
                    groupName -> {
                        groups.filterByName(groupName)
                                .forEachClient(
                                        (group, clientBuilder) -> {
                                            // Add Spring's OAuth2 interceptor
                                            var oauth2Interceptor =
                                                    new OAuth2ClientHttpRequestInterceptor(
                                                            authorizedClientManager);
                                            oauth2Interceptor.setClientRegistrationIdResolver(
                                                    request -> groupName);
                                            clientBuilder.requestInterceptor(oauth2Interceptor);
                                        });
                    });
        };
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
