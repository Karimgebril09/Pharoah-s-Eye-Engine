package com.example.testforcrud.Query;
import lombok.Data;
public class Query {

    private final String query;
    public Query(String query) {
        this.query = query;
    }
    public String getQuery() {
        return query;
    }
}
