package com.example.springapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MyService {
    @Value("${my.custom.string}")
    private String myString;

    // ... other methods
}