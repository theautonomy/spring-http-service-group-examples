package com.example.demo.model;

import java.util.Map;

public record ApiObjectRequest(String name, Map<String, Object> data) {}
