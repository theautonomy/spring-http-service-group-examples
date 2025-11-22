package com.example.demo.config;

import com.example.demo.client.jph.JsonPlaceholderClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "jph",
        types = {JsonPlaceholderClient.class})
// @ImportHttpServices( group = "ara", types = {RestfulApiClient.class})
@Import(MyHttpServiceRegistrar.class)
public class HttpClientConfig {
    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> {
            groups.filterByName("ara")
                    .forEachClient(
                            (group, clientBuilder) -> {
                                clientBuilder.requestInterceptor(new LoggingInterceptor());
                            });
        };
    }
}
