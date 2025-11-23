package com.example.demo.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

import com.example.demo.model.ObjectSearch;

/**
 * Custom argument resolver that converts ObjectSearch parameter into query parameters.
 *
 * <p>Example usage:
 *
 * <pre>
 * &#64;GetExchange("/objects")
 * List&lt;ApiObject&gt; searchObjects(ObjectSearch search);
 * </pre>
 *
 * <p>When called with ObjectSearch.of("MacBook", "Silver"), it converts to:
 *
 * <pre>
 * GET /objects?name=MacBook&color=Silver
 * </pre>
 */
public class ObjectSearchArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(
            Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {

        // Check if this parameter is of type ObjectSearch
        if (!parameter.getParameterType().equals(ObjectSearch.class)) {
            return false; // Not handled by this resolver
        }

        // Cast and extract search parameters
        ObjectSearch search = (ObjectSearch) argument;

        // Add query parameters
        if (search.name() != null && !search.name().isEmpty()) {
            requestValues.addRequestParameter("name", search.name());
        }
        if (search.color() != null && !search.color().isEmpty()) {
            requestValues.addRequestParameter("color", search.color());
        }

        return true; // Argument was handled
    }
}
