package com.example.demo.runner;

import java.beans.PropertyDescriptor;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.autoconfigure.HttpClientProperties;
import org.springframework.boot.http.client.autoconfigure.HttpClientsProperties;
import org.springframework.boot.http.client.autoconfigure.imperative.ImperativeHttpClientsProperties;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanListRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final HttpClientSettings httpClientSettings;
    private final HttpClientsProperties httpClientsProperties;
    private final ImperativeHttpClientsProperties imperativeHttpClientsProperties;
    private final HttpServiceClientProperties httpServiceClientProperties;

    public BeanListRunner(
            ApplicationContext applicationContext,
            HttpClientSettings httpClientSettings,
            HttpClientsProperties httpClientsProperties,
            ImperativeHttpClientsProperties imperativeHttpClientsProperties,
            HttpServiceClientProperties httpServiceClientProperties) {
        this.applicationContext = applicationContext;
        this.httpClientSettings = httpClientSettings;
        this.httpClientsProperties = httpClientsProperties;
        this.imperativeHttpClientsProperties = imperativeHttpClientsProperties;
        this.httpServiceClientProperties = httpServiceClientProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        try (PrintWriter writer = new PrintWriter(new FileWriter("spring-beans.txt"))) {
            Arrays.stream(beanNames)
                    .map(
                            name -> {
                                Object bean = applicationContext.getBean(name);
                                return name + " - " + bean.getClass().getName();
                            })
                    .sorted(
                            (a, b) -> {
                                String typeA = a.substring(a.indexOf(" - ") + 3);
                                String typeB = b.substring(b.indexOf(" - ") + 3);
                                return typeA.compareTo(typeB);
                            })
                    .forEach(writer::println);
        }

        System.out.println(
                "\n=== Spring Beans list written to spring-beans.txt ("
                        + beanNames.length
                        + " beans) ===\n");

        printHttpClientBeanDetails();
    }

    private void printHttpClientBeanDetails() {
        System.out.println("\n=== HTTP Client Bean Details ===\n");

        // HttpClientSettings
        System.out.println("1. HttpClientSettings:");
        System.out.println("   connectTimeout: " + httpClientSettings.connectTimeout());
        System.out.println("   readTimeout: " + httpClientSettings.readTimeout());
        System.out.println("   sslBundle: " + httpClientSettings.sslBundle());

        // HttpClientsProperties
        System.out.println("\n2. HttpClientsProperties (spring.http.clients):");
        System.out.println("   connectTimeout: " + httpClientsProperties.getConnectTimeout());
        System.out.println("   readTimeout: " + httpClientsProperties.getReadTimeout());
        System.out.println("   ssl:");
        System.out.println("      bundle: " + httpClientsProperties.getSsl().getBundle());
        System.out.println("   redirects: " + httpClientsProperties.getRedirects());

        // ImperativeHttpClientsProperties
        System.out.println(
                "\n3. ImperativeHttpClientsProperties (spring.http.clients.imperative):");
        printBeanProperties(imperativeHttpClientsProperties);

        // HttpServiceClientProperties - it extends Map<String, HttpClientProperties>
        System.out.println("\n4. HttpServiceClientProperties (spring.http.serviceclient):");
        Map<String, HttpClientProperties> groups = httpServiceClientProperties;
        if (groups != null && !groups.isEmpty()) {
            groups.forEach(
                    (groupName, props) -> {
                        System.out.println("\n   Group: " + groupName);
                        System.out.println("      baseUrl: " + props.getBaseUrl());
                        System.out.println("      connectTimeout: " + props.getConnectTimeout());
                        System.out.println("      readTimeout: " + props.getReadTimeout());
                        System.out.println("      redirects: " + props.getRedirects());
                        if (props.getSsl() != null) {
                            System.out.println("      ssl.bundle: " + props.getSsl().getBundle());
                        }
                    });
        } else {
            System.out.println("   (no groups configured)");
        }

        System.out.println("\n=== HTTP Client Bean Details End ===\n");
    }

    private void printBeanProperties(Object bean) {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(bean.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getReadMethod() != null && !"class".equals(descriptor.getName())) {
                try {
                    Object value = descriptor.getReadMethod().invoke(bean);
                    System.out.println("   " + descriptor.getName() + ": " + value);
                } catch (Exception e) {
                    System.out.println("   " + descriptor.getName() + ": (error reading)");
                }
            }
        }
    }
}
