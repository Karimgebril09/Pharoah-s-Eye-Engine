import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoWriteException;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

public class testformongo {
    public static void main(String[] args) {
        MongoClient client = MongoClients.create("");
        MongoDatabase db = client.getDatabase("test");

        MongoCollection col = db.getCollection("documents");
        ArrayList<String> words = new ArrayList<>();
        words.add("word1");
        words.add("word2");
        words.add("word3");
        ArrayList<String> words2 = new ArrayList<>();
        words.add("word5");
        words.add("word2");
        words.add("word3");

        Document doc = new Document("__id", "3").append("url", "http://example.commmm").append("popularity", 9).append("Words", words2);

       // col.insertOne(sampleDoc);
        try {
            col.insertOne(doc); // Attempt to insert the document
            System.out.print("g");
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) { // Duplicate key error code
                // Handle duplicate key error (e.g., retry insertion)
                // You can implement retry logic here
                System.out.println("Duplicate key error. Retrying insertion...");
                // Retry insertion after a brief delay (e.g., 1 second)
                //Thread.sleep(1000);
                col.insertOne(doc); // Retry insertion
            } else {
                // Handle other types of write errors
                System.out.println("Error: " + e.getMessage());
            }
        }
       // Document sampleDoc = new Document("_id", "2").append("name", "John Smith");

       // col.insertOne(sampleDoc);


    }
}
