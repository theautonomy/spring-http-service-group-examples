package com.example.demo.config;

import com.example.demo.client.ara.RestfulApiClient;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.service.registry.AbstractHttpServiceRegistrar;

public class MyHttpServiceRegistrar extends AbstractHttpServiceRegistrar {

    @Override
    protected void registerHttpServices(GroupRegistry registry, AnnotationMetadata metadata) {
        // registry.forGroup("echo").register(EchoServiceA.class, EchoServiceB.class);
        registry.forGroup("ara").detectInBasePackages(RestfulApiClient.class);
    }
}
