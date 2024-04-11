import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.Optional;

public class Database {

    public static ObjectId insertDocument(MongoCollection<Document> col, String url, Optional<Integer> popularity) {
        ObjectId newId;
        boolean inserted = false;

        try {
            while (!inserted) {
                newId = new ObjectId();
                Document existingDoc = col.find(Filters.eq("_id", newId)).first();
                if (existingDoc == null) {
                    Document doc = new Document("_id", newId)
                            .append("url", url)
                            .append("popularity", popularity.orElse(null));

                    col.insertOne(doc);
                    System.out.println("Document inserted successfully");
                    inserted = true;
                    return newId;
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during database operations
            e.printStackTrace();
        }

        return null; // Return null if insertion failed
    }
    public static ObjectId insert_DocWordData(MongoCollection<Document> col,ArrayList<Integer> Positions,float tf,ObjectId Docid) {
        ObjectId newId = null;
        boolean Inserted = false;
        while (!Inserted) {
            newId = new ObjectId();
            Document existingDoc = col.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("Docid",Docid).append("tf",tf).append("Positions", Positions);

                col.insertOne(doc);
                System.out.println("Word_Doc inserted successfully");
                Inserted = true;
            }
        }
        return newId;
    }
    public static Document getWordDoc(MongoCollection<Document> col, ObjectId Id) {
        try {
            Document document = col.find(new Document("_id", Id)).first();
            return document;
        } catch (MongoException e) {
            System.err.println("Error retrieving document: " + e.getMessage());
            return null;
        }
    }

    public static void Wordinsertion(MongoCollection<Document> col, String Word, int docsCount, double idf, ArrayList<ObjectId> ArrayOfdocs) {

        ObjectId newId;
        boolean Inserted = false;
        while (!Inserted) {
            newId = new ObjectId();
            Document existingDoc = col.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("word",Word).append("DocsCount",docsCount).append("IDF",idf).append("ArrayOfdocs",ArrayOfdocs);
                col.insertOne(doc);
                System.out.println("Word inserted successfully");
                Inserted = true;
            }
        }
    }




}
