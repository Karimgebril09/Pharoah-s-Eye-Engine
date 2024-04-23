package com.example.springapi.service;

import com.example.springapi.api.model.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    public void processQuery(Query query) {
        // Use the query.getQuery() method to access the received string
        String receivedQuery = query.getQuery();
        // Implement logic to process the receivedQuery string
        System.out.println("Received query: " + receivedQuery);  // Example processing
    }
}