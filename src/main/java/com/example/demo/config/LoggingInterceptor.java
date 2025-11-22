package com.example.demo.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        System.out.println("\n=== HTTP Request ===");
        System.out.println("Method: " + request.getMethod());
        System.out.println("URI: " + request.getURI());
        System.out.println("Headers: " + request.getHeaders());
        if (body.length > 0) {
            System.out.println("Body: " + new String(body, StandardCharsets.UTF_8));
        }
        System.out.println("====================\n");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        System.out.println("\n=== HTTP Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Status Text: " + response.getStatusText());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("=====================\n");
    }
}
