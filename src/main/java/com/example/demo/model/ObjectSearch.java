package com.example.demo.model;

/**
 * Custom parameter type for searching API objects. Demonstrates HttpServiceArgumentResolver
 * pattern.
 */
public record ObjectSearch(String name, String color) {

    public static ObjectSearch of(String name, String color) {
        return new ObjectSearch(name, color);
    }
}
