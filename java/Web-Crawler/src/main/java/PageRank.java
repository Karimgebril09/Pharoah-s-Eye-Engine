import java.util.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class PageRank {
    public static MongoDatabase db;
    public static MongoCollection<org.bson.Document> pageRankCollection;
    public static   Link[] links;
    static class Link {
        String url;
        double pageRank;
        List<String> outgoingLinks = new ArrayList<>();
        public Link(String url) {
            this.url = url;
            // Initialize pageRank to 1.0
            this.pageRank = 1.0;
        }
        @Override
        public String toString() {
            return url + " : " + pageRank;
        }

    }
    public static MongoClient createConnection() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        try {
            MongoClient client = MongoClients.create(connectionString);
            return client;
        } catch (MongoException e) {
            System.err.println("Error creating connection: " + e.getMessage());
            return null;
        }
    }
    public static Link[] collect(MongoClient client) {
        try {
            try (client) {
                db = client.getDatabase("Salma");
                MongoCollection<org.bson.Document> collection = db.getCollection("Ranker");
                List<Link> alllinks = new ArrayList<>();
                for (org.bson.Document doc : collection.find()) {
                    String url = doc.getString("url");
                    Link link = new Link(url);
                    // Retrieve outgoing links array from the document
                    List<String> outgoingUrls = (List<String>) doc.get("outgoings");
                    for (String outgoingUrl : outgoingUrls) {
                        link.outgoingLinks.add(outgoingUrl);
                    }
                    // Add the created Link object to the list
                    alllinks.add(link);
                }

                // Convert list to array
                Link[] linksArray = new Link[alllinks.size()];
                linksArray = alllinks.toArray(linksArray);

                return linksArray;
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving documents: " + e.getMessage());
            return null;
        }
    }

    // Function to calculate PageRank for a set of links
    public static void calculatePageRank(int iterations) {
        int N = links.length;
        System.out.println("N: " + N);
        // Perform iterations
        for (int iter = 0; iter < iterations; iter++) {
            // Array to store temporary PageRank values
            double[] newPageRanks = new double[N];

            // Calculate new PageRank values for each link
            for (int i = 0; i < N; i++) {
                double sum = 0.0;
                for (Link incomingLink : links) {
                   // System.out.println(incomingLink);
                    if (incomingLink.outgoingLinks.contains(links[i].url)) {
                        // PageRank equation: PR(p) = (1-d) + d * (PR(q1)/L(q1) + ... + PR(qn)/L(qn))
                      //  System.out.println("inside");
                        sum += incomingLink.pageRank / incomingLink.outgoingLinks.size();
                    }
                }
                // Update PageRank value
                newPageRanks[i] = (1 - 0.85) + 0.85 * sum;
            }

            // Update PageRank values
            for (int i = 0; i < N; i++) {
                links[i].pageRank = newPageRanks[i];
            }
        }
    }
    public static void updatePopularity(MongoClient client, String url, double newPopularity) {
        try {
            db = client.getDatabase("test");
            MongoCollection<Document> collection = db.getCollection("documents");

            Document foundDoc = collection.find(eq("url", url)).first();
            if (foundDoc != null) {
                // Update the "popularity" field of the found document
                collection.updateOne(eq("url", url), new Document("$set", new Document("popularity", newPopularity)));
               // System.out.println("Popularity updated for URL: " + link.url);
               // System.out.println("Popularity updated for URL: " + url);
            } else {
                System.out.println("Document with URL " + url + " not found.");
            }
        } catch (MongoException e) {
            System.err.println("Error updating popularity values: " + e.getMessage());
        }
    }
    // Main method to test the PageRank algorithm
    public static void main(String[] args) {
        MongoClient client = createConnection();

        // Retrieve links from MongoDB collection
        links = collect(client);

        if (links != null) {
            // Constants
            final double DAMPING_FACTOR = 0.85; // Damping factor (typical value)

            // Calculate PageRank
            calculatePageRank(100); // 10 iterations for convergence

            // Output PageRank values
            for (Link link : links) {
                client = createConnection();
                updatePopularity(client, link.url, link.pageRank);
               /* System.out.println("URL: " + link.url);
                System.out.println("PageRank: " + link.pageRank);*/
                System.out.println("************************************");
            }
        } else {
            System.out.println("Failed to retrieve links from MongoDB collection.");
        }
    }
}
