package com.example.springapi.api.controller;

import com.example.springapi.api.model.Query;
import com.example.springapi.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

    private QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")  // Map to a POST request at /query
    public String receiveQuery(@RequestBody Query receivedQuery) {
        // Process the received Query object
        String query = receivedQuery.getQuery();
        queryService.processQuery(receivedQuery);  // Call service to process the query
        return "Query received: " + query;
    }
}
