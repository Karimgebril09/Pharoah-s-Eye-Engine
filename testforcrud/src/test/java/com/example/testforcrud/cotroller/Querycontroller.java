package com.example.testforcrud.cotroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.testforcrud.Query.Query;
/*import com.example.testforcrud.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Query")
public class Querycontroller {
    private QueryService queryService;
    @Autowired
    public Querycontroller(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public void saveQuery(@RequestBody Query request) {
        String query = request.getQuery();
        // Save the query to the database using your repository
        queryRepository.save(query);
    }
}*/
