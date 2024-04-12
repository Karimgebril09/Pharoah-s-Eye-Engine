import static com.mongodb.client.model.Filters.eq;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.Optional;

public class Database {
    public MongoDatabase db;
    private MongoClient client1;
    public MongoCollection<Document> DocCollection;
    public MongoCollection<Document> WordsCollection;
    public MongoCollection<Document> WordDocCollection;

    public void initDataBase() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");

        client1 = MongoClients.create(connectionString);
        db = client1.getDatabase("test");
        DocCollection = db.getCollection("documents");
        WordsCollection = db.getCollection("Words");
        WordDocCollection = db.getCollection("Word_Document");

        System.out.println("Connected to database");

        // Create unique index on the "_id" field
        IndexOptions indexOptions = new IndexOptions();
        DocCollection.createIndex(Indexes.ascending("_id"), indexOptions);
        WordsCollection.createIndex(Indexes.ascending("_id"), indexOptions);
        WordDocCollection.createIndex(Indexes.ascending("_id"), indexOptions);
        System.out.println(DocCollection);
        }

    public ObjectId insertDocument(String url, Optional<Integer> popularity) {
        ObjectId newId = null;
        boolean isUniqueId = false;
        while (!isUniqueId) {
            newId = new ObjectId();
            Document existingDoc = DocCollection.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("_id", newId);
                doc.append("url", url);
                // Check if popularity is present, then insert it, otherwise insert null
                popularity.ifPresentOrElse(
                        pop -> doc.append("popularity", pop),
                        () -> doc.append("popularity", null)
                );

                DocCollection.insertOne(doc);
                isUniqueId = true;
            }
        }
        return newId;
    }
    public ObjectId insert_DocWordData(ArrayList<Integer> Positions,float tf,ObjectId Docid) {
        ObjectId newId = null;
        boolean Inserted = false;
        while (!Inserted) {
            newId = new ObjectId();
            Document existingDoc = WordDocCollection.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("_id",newId).append("Docid",Docid).append("tf",tf).append("Positions", Positions);

                WordDocCollection.insertOne(doc);
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

    public void Wordinsertion( String Word, int docsCount, double idf, ArrayList<ObjectId> ArrayOfdocs) {

        ObjectId newId;
        boolean Inserted = false;
        while (!Inserted) {
            newId = new ObjectId();
            Document existingDoc = WordsCollection.find(new Document("_id", newId)).first();
            if (existingDoc == null) {
                Document doc = new Document();
                doc.append("word",Word).append("DocsCount",docsCount).append("IDF",idf).append("ArrayOfdocs",ArrayOfdocs);
                WordsCollection.insertOne(doc);
                Inserted = true;
            }
        }
    }
}
