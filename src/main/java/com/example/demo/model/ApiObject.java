package com.example.demo.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiObject(
        String id,
        String name,
        Map<String, Object> data,
        @JsonProperty("createdAt") String createdAt,
        @JsonProperty("updatedAt") String updatedAt) {}
