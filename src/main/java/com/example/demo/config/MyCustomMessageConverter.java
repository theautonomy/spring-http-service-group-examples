package com.example.demo.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Simple custom HTTP message converter example.
 *
 * <p>This converter handles plain text and demonstrates the basic message converter pattern.
 */
public class MyCustomMessageConverter extends AbstractHttpMessageConverter<String> {

    public MyCustomMessageConverter() {
        super(
                StandardCharsets.UTF_8,
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_HTML,
                MediaType.APPLICATION_JSON); // Add JSON support to see converter in action
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        byte[] bytes = inputMessage.getBody().readAllBytes();
        String content = new String(bytes, StandardCharsets.UTF_8);

        System.out.println("[Mycustomconverter] read: " + content.length() + " bytes");

        // Simple conversion logic: Add metadata prefix and transform first line
        String[] lines = content.split("\n", 2);
        String firstLine = lines.length > 0 ? lines[0] : content;
        String rest = lines.length > 1 ? lines[1] : "";

        // Transform: Make first line uppercase and add conversion marker
        String transformedFirstLine =
                "<!-- CONVERTED BY MyCustomMessageConverter -->\n" + firstLine.toUpperCase();
        String convertedContent =
                rest.isEmpty() ? transformedFirstLine : transformedFirstLine + "\n" + rest;

        System.out.println("[MyCustomConverter] Converted first line to uppercase");
        return convertedContent;
    }

    @Override
    protected void writeInternal(String content, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        System.out.println("[MyCustomConverter] Write: " + content.length() + " bytes");

        // Simple conversion logic: Add timestamp prefix
        String timestamp = java.time.LocalDateTime.now().toString();
        String convertedContent = "<!-- Written at: " + timestamp + " -->\n" + content;

        System.out.println("[MyCustomConverter] Added timestamp prefix");
        outputMessage.getBody().write(convertedContent.getBytes(StandardCharsets.UTF_8));
    }
}
