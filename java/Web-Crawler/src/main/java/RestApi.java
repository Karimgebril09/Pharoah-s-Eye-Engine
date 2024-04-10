/*import spark.Spark.*;

public class RestApi {

    public static void main(String[] args) {
        port(8080); // Set the port for your Spark application

        post("/search", (req, res) -> {
            String query = req.body(); // Get the query from the request body
            // Process the query here
            String processedQuery = processQuery(query);
            // Return the search results
            return "Search results for: " + processedQuery;
        });
    }

    private static String processQuery(String query) {
        // Implement your query processing logic here
        return query.toUpperCase(); // For example, just converting query to uppercase
    }
}*/
