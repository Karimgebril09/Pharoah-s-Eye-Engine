import static com.mongodb.client.model.Filters.eq;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;



public class testformongo {
    public static void main(String[] args) {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        //MongoClient client;
        //client = MongoClients.create(connectionString);

        try (MongoClient client = MongoClients.create(connectionString)) {
            MongoDatabase db = client.getDatabase("test");
            MongoCollection<Document> col = db.getCollection("documents");
            MongoCollection<Document> col2 = db.getCollection("Words");
            System.out.println("Connected to database");

            // Create unique index on the "_id" field
            IndexOptions indexOptions = new IndexOptions();
            col.createIndex(Indexes.ascending("_id"), indexOptions);

            // Create and insert documents
            ArrayList<String> words = new ArrayList<>();
            words.add("word1");
            words.add("word2");
            words.add("word3");
            ArrayList<String> words2 = new ArrayList<>();
            words2.add("word5");
            words2.add("word6");
            words2.add("word9");

            insertDocument(col, "https://example.commmm", Optional.of(6), words);
            insertDocument(col, "https://example2.commmm", Optional.of(6), words2);

            System.out.println("Documents inserted successfully");
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                System.out.println("Duplicate key error. Retrying insertion...");
                // Handle duplicate key error here
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }



    private static void insertDocument(MongoCollection<Document> col, String url, Optional<Integer> popularity, List<String> words) {
        ObjectId newId;
        boolean isUniqueId = false;

        while (!isUniqueId) {
            newId = new ObjectId();
            Document existingDoc = col.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("_id", newId);
                doc.append("url", url);
                // Check if popularity is present, then insert it, otherwise insert null
                popularity.ifPresentOrElse(
                        pop -> doc.append("popularity", pop),
                        () -> doc.append("popularity", null)
                );
                doc.append("Words", words);

                col.insertOne(doc);
                System.out.println("Document inserted successfully");
                isUniqueId = true;
            }
        }
    }

}
