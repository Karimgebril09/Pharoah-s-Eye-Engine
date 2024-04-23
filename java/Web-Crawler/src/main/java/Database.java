import static com.mongodb.client.model.Filters.eq;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Database {
    public MongoDatabase db;
    private MongoClient client1;
    public MongoCollection<Document> DocCollection;
    public static MongoCollection<Document> WordsCollection;
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


    /*
     * 1- call in main get all prev words
     * 2- get init Number
     * 3-check in while that the url doesn't exist in db
     * */
    public void Wordinsertion( String Word, int docsCount, double idf, ArrayList<ObjectId> ArrayOfdocs) {
        Document DocExist = WordsCollection.find(new Document("word", Word)).first();
        if(DocExist==null)
        {
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
        else
        {
            WordsCollection.updateOne(
                    new Document("word", Word),
                    new Document("$set", new Document("DocsCount", docsCount))
            );
            ArrayList<ObjectId> existingArray = (ArrayList<ObjectId>) DocExist.get("ArrayOfdocs");
            if(ArrayOfdocs!=null) {
                existingArray.addAll(ArrayOfdocs);
            }
            WordsCollection.updateOne(
                    new Document("word", Word),
                    new Document("$set", new Document("ArrayOfdocs", existingArray))
            );
            WordsCollection.updateOne(
                    new Document("word", Word),
                    new Document("$set", new Document("IDF", idf))
            );
        }
    }


    public double getInitNumberOfPages()
    {
        return (double) DocCollection.countDocuments();
    }

    public HashMap<String,Integer> getallPreviousWords() {
        // Initialize a HashMap to store words and their document counts
        HashMap<String, Integer> wordDocsCountTemp = new HashMap<>();
        // Retrieve all documents from the collection
        FindIterable<Document> documents = WordsCollection.find();
        int NumberOfPages=0;
        // Iterate over the documents and extract word and count
        try (MongoCursor<Document> cursor = documents.iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();

                // Retrieve 'Word' and 'count' from the document and add to the HashMap
                String word = document.getString("word");
                Integer count = document.getInteger("DocsCount");

                if (word != null && count != null) {
                    wordDocsCountTemp.put(word, count);
                }
            }
        }
        return wordDocsCountTemp;
    }
    public boolean DoesUrlExist(String url)
    {
        try {
            Document document = DocCollection.find(new Document("url", url)).first();
            return document!=null;
        } catch (MongoException e) {
            System.err.println("Error retrieving document: " + e.getMessage());
            return true;
        }
    }
}
