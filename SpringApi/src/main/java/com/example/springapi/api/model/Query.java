package com.example.springapi.api.model;

import org.springframework.stereotype.Component;

@Component  // Use @Component or @Entity if applicable
public class Query {
    private String query;
    public Query(String query) {
        this.query = query;
    }
    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
}