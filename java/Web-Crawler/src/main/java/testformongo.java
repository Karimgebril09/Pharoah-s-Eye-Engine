import static com.mongodb.client.model.Filters.eq;

import com.mongodb.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.*;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;



public class testformongo {
    public static MongoCollection<org.bson.Document> WordDocCollection;
    public static void main(String[] args) {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        //MongoClient client;
        //client = MongoClients.create(connectionString);

        try (MongoClient client = MongoClients.create(connectionString)) {
            MongoDatabase db = client.getDatabase("test");

            WordDocCollection = db.getCollection("Word_Document");
            //MongoCollection<Document> col2 = db.getCollection("Words");
            System.out.println("Connected to database");

            // Create unique index on the "_id" field
            //IndexOptions indexOptions = new IndexOptions();
            //col.createIndex(Indexes.ascending("_id"), indexOptions);

            // Create and insert documents
            ArrayList<String> words = new ArrayList<>();
            words.add("word1");
            words.add("word2");
            words.add("word3");
            ArrayList<String> words2 = new ArrayList<>();
            words2.add("word5");
            words2.add("word6");
            words2.add("word9");
            ArrayList<Integer> pos = new ArrayList<Integer>();
            pos.addAll(Arrays.asList(10, 25, 42));



            //insertDocument(col,  Optional.of(6), pos);
            //insertDocument(col, "https://example2.commmm", Optional.of(6), words2);
            ObjectId id = new ObjectId("66172eb9eb21ba702f7a9a37");
            Document document=getWordDoc(WordDocCollection,id);
            HashMap<String, Object> DocWordData=new HashMap<>(document);
            System.out.println(DocWordData);

            //System.out.println("Documents inserted successfully");
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                System.out.println("Duplicate key error. Retrying insertion...");
                // Handle duplicate key error here
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static HashMap<String, Object> DocWordData(Document document) {
        HashMap<String, Object> details = new HashMap<>();
        details.put("_id", document.getObjectId("_id"));

        // Access IDF as a number (assuming it's stored as a double)
        Integer Docid = document.getInteger("Docid");
        details.put("Docid", Docid);
        Integer tf = document.getInteger("tf");
        details.put("tf", tf);

        // Access the ArrayList of integers (assuming it's stored as an array)
        List<Integer> Positions = (List<Integer>) document.get("Positions");
        details.put("Positions", Positions);

        // Add more details as needed based on your document schema
        return details;
    }
    public static Document getWordDoc(MongoCollection<Document> col, ObjectId id) {
        try {
            Document document = col.find(new Document("_id", id)).first();
            if (document == null) {
                System.err.println("Document not found with ID: " + id);
                return null;  // Or return an empty HashMap as needed
            }
            return document;
        } catch (MongoException e) {
            System.err.println("Error retrieving document: " + e.getMessage());
            return null;
        }
    }
    private static void insertDocument(MongoCollection<Document> col,  Optional<Integer> popularity, List<Integer> words) {
        ObjectId newId;
        boolean isUniqueId = false;

        while (!isUniqueId) {
            newId = new ObjectId();
            Document existingDoc = col.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("_id", newId);
                ObjectId newIddoc;
                newIddoc = new ObjectId();
                doc.append("Docid", newId);
                // Check if popularity is present, then insert it, otherwise insert null
                popularity.ifPresentOrElse(
                        pop -> doc.append("tf", pop),
                        () -> doc.append("tf", null)
                );
                doc.append("Positions", words);

                col.insertOne(doc);
                System.out.println("Document inserted successfully");
                isUniqueId = true;
            }
        }
    }

}
