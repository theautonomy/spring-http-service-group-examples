package com.example.demo.client.httpbin;

import com.example.demo.model.BasicAuthResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface HttpBinClient {

    @GetExchange("/basic-auth/{user}/{password}")
    BasicAuthResponse testBasicAuth(@PathVariable String user, @PathVariable String password);

    /**
     * Get HTML content to test custom message converter. Returns plain HTML text that will be
     * processed by MyCustomMessageConverter.
     */
    @GetExchange("/html")
    String getHtml();

    /**
     * Get UUID as plain text to test custom message converter. Returns a UUID string in plain text
     * format.
     */
    @GetExchange("/uuid")
    String getUuid();
}
