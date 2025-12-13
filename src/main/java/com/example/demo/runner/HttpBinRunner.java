package com.example.demo.runner;

import com.example.demo.client.httpbin.HttpBinClient;
import com.example.demo.model.BasicAuthResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class HttpBinRunner implements CommandLineRunner {

    private final HttpBinClient thisClient;

    // Since we have two HttpBinClient, we need to qualify it
    public HttpBinRunner(@Qualifier("httpBinClient") HttpBinClient thisBinClient) {
        this.thisClient = thisBinClient;
    }

    @Override
    public void run(String... args) throws Exception {
        // ===== HTTP Basic Authentication Test =====
        System.out.println("\n=== Testing HTTP Basic Authentication ===\n");
        System.out.println("   Testing httpbin.org basic auth endpoint with credentials:");
        System.out.println("   Username: mark");
        System.out.println("   Password: secret\n");

        // Test: HTTP Basic Authentication
        System.out.println("1. Calling /basic-auth/mark/secret with HTTP Basic Auth:");
        try {
            BasicAuthResponse authResponse = thisClient.testBasicAuth("mark", "secret");
            System.out.println("   ✓ Authentication successful!");
            System.out.println("   Authenticated: " + authResponse.authenticated());
            System.out.println("   User: " + authResponse.user());
        } catch (Exception e) {
            System.out.println("   ✗ Authentication failed: " + e.getMessage());
        }

        System.out.println("\n=== HTTP Basic Authentication test completed! ===\n");

        // ===== Custom Message Converter Test =====
        System.out.println("\n=== Testing Custom Message Converter ===\n");
        System.out.println("   MyCustomMessageConverter handles text/plain and text/html");
        System.out.println("   Watch for [MyCustomConverter] log messages in the output\n");

        // Test 1: Get UUID (plain text)
        System.out.println("1. Calling /uuid (returns plain text):");
        try {
            String uuid = thisClient.getUuid();
            System.out.println("   ✓ UUID received (check logs for converter message)");
            System.out.println("   UUID: " + uuid.trim());
        } catch (Exception e) {
            System.out.println("   ✗ Failed: " + e.getMessage());
        }

        // Test 2: Get HTML
        System.out.println("\n2. Calling /html (returns HTML text):");
        try {
            String html = thisClient.getHtml();
            int htmlLength = html.length();
            System.out.println(html);
            System.out.println("   ✓ HTML received (check logs for converter message)");
            System.out.println("   HTML length: " + htmlLength + " bytes");
            System.out.println(
                    "   Preview: "
                            + html.substring(0, Math.min(80, htmlLength)).replaceAll("\\n", " ")
                            + "...");
        } catch (Exception e) {
            System.out.println("   ✗ Failed: " + e.getMessage());
        }

        System.out.println("\n=== Custom Message Converter test completed! ===\n");

        // ===== GitHub User Service Tests =====
        System.out.println("\n=== GitHub User Service (OAuth2) ===\n");
        System.out.println(
                "   GitHub OAuth2 requires browser-based authentication (Authorization Code flow).");
        System.out.println(
                "   OAuth2 cannot be tested in CommandLineRunner without a web context.\n");
        System.out.println("   To test GitHub OAuth2 integration:");
        System.out.println(
                "   1. Make sure your OAuth2 credentials are configured in application.properties");
        System.out.println("   2. Start the application (it should be running now)");
        System.out.println(
                "   3. Open your browser and visit: http://localhost:8080/api/github/user");
        System.out.println("   4. You'll be redirected to GitHub to authorize");
        System.out.println("   5. After authorization, you'll see your GitHub user info\n");
        System.out.println("=== GitHub OAuth2 endpoint available at /api/github/user ===\n");

        System.out.println("\n=== All tests completed! ===\n");
    }
}
