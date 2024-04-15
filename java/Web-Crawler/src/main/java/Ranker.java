/*import java.io.BufferedReader;
import java.io.FileReader;*/
import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.HashMap;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.model.Sorts;
public class Ranker {
    public static Set<String> stopWords = new HashSet<>();
    public static MongoDatabase db;
    public static MongoDatabase db2;
    public static MongoCollection<org.bson.Document> DocCollection;
    public static MongoCollection<org.bson.Document> WordsCollection;
    public static MongoCollection<org.bson.Document> WordDocCollection;
    public static MongoCollection<org.bson.Document> Query;
    public static Database DBhandler;
    public static ArrayList<HashMap<String, Object>> wordColofQuery = new ArrayList<>();
    public static HashMap<ObjectId, Double> scoreshashmap = new HashMap<>();
    public static void main(String[] args) throws IOException {
        String query ; ///to be modifed get from react
        //ArrayList<String> afterProcessing = QueryProcessing(Query);
        String path="src/main/java/Stopwords.txt";//Step1 extracting StopWords
        try {
            getStopwords(path);
            //////////////////////////////////////////////tested before calling the database
            MongoClient client = createConnection();
            if (client == null) {
                System.err.println("Failed to create connection");
                return;
            }
            Document queryDocument = getLastInsertedQuery(client);
            query=extractQuery(queryDocument);
            System.out.println(query);
            ArrayList<String> afterProcessing = queryProcessing(query);
            System.out.println(afterProcessing);
            //HashSet<String> uniqueTerms = new HashSet<>(afterProcessing);
            //System.out.println(uniqueTerms);
           // client = createConnection();
            System.out.println("passed");
           /* ObjectId id = new ObjectId("6619c365fb496a42743a0391");
            ObjectId did = new ObjectId("6619c365fb496a42743a0390");
            Document document = getWordDoc2(client, id,did);
            HashMap<String, Object> DocWordDataa=new HashMap<>(document);
            System.out.println(DocWordDataa.get("tf"));*/
            //ObjectId id = new ObjectId("6619c365fb496a42743a0390");
            //client = createConnection();
            Document document;
            HashMap<String, Object> wordData;
            for (String token : afterProcessing) {
                client = createConnection();
                if(token !=null){
                document = getWord(client, token);

                if (document != null) {
                    //System.out.println( );
                    //HashMap<String, Object> DocWordData = DocWordData(document);
                    //System.out.println(DocWordData);
                    //HashMap<String, Object> DocData = docData(document);
                    //System.out.println(DocData.get("url"));
                    wordData = wordData(document);
                    System.out.println(wordData.get("_id"));
                    wordColofQuery.add(wordData);

                } else {
                    System.out.println("this word is not in the indexer");
                }}

            }
            double score;
            HashMap<ObjectId, Double> temphash = new HashMap<>();
            HashMap<String, Object> temphash2;

            for (HashMap<String, Object> wordDataMap : wordColofQuery) {
                List<ObjectId> arrayOfDocs = (List<ObjectId>) wordDataMap.get("ArrayOfdocs");
                if (arrayOfDocs != null) {
                    for (ObjectId temp : arrayOfDocs) {
                        client = createConnection();
                        Document doc = getWordDoc(client, temp);
                        if (doc != null) {
                            // Populate temphash2 with data from the document
                            temphash2 = DocWordData(doc);
                            score = (Double) wordDataMap.get("IDF") * (Double) temphash2.get("tf");
                            ObjectId docId = (ObjectId) temphash2.get("Docid");
                            double currentScore = temphash.getOrDefault(docId, 0.0);
                            temphash.put(docId, currentScore + score);
                        }
                    }
                } else {
                    System.err.println("ArrayOfdocs is null in wordDataMap: " + wordDataMap);
                }
                System.out.println("passed");
            }

            // Print temphash
            for (Map.Entry<ObjectId, Double> entry : temphash.entrySet()) {
                ObjectId key = entry.getKey();
                Double value = entry.getValue();
                System.out.println(key + ": " + value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public static Document getWordDoc(MongoClient client, ObjectId id) {
        try (client) {  // Ensure connection is closed after use
            db = client.getDatabase("test");
            WordDocCollection = db.getCollection("Word_Document");  // Not needed here
            System.out.println(WordDocCollection.countDocuments());
            Document document = WordDocCollection.find(new Document("_id", id)).first();
            if (document == null) {
                System.err.println("Document not found with ID: " + id);
                return null;
            }
            return document;
        } catch (MongoException e) {
            System.err.println("Error retrieving document: " + e.getMessage());
            return null;
        }
    }
    public static Document getWordDoc2(MongoClient client, ObjectId id, ObjectId docid) {
        try (client) {  // Ensure connection is closed after use
            db = client.getDatabase("test");
            WordDocCollection = db.getCollection("Word_Document");  // Not needed here
            System.out.println(WordDocCollection.countDocuments());
            // Build the query with both _id and docid conditions
            Document query = new Document("_id", id).append("Docid", docid);

            Document document = WordDocCollection.find(query).first();
            if (document == null) {
                System.err.println("Document not found with ID: " + id + " and Docid: " + docid);
                return null;
            }
            return document;
        } catch (MongoException e) {
            System.err.println("Error retrieving document: " + e.getMessage());
            return null;
        }
    }
    public static Document getWord(MongoClient client, String word) {
        try (client) {  // Ensure connection is closed after use
            db = client.getDatabase("test");
            WordsCollection = db.getCollection("Words");
           // System.out.println(WordsCollection.countDocuments());
            Document document = WordsCollection.find(new Document("word", word)).first(); // Update the query
            if (document == null) {
                System.out.println("Document not found with word: " + word);
                return null;
            }
            System.out.println("Document was found with word: " + word);
            return document;
        } catch (MongoException e) {
            System.out.println("Error retrieving document: " + e.getMessage());
            return null;
        }
    }
    public static Document getDoc(MongoClient client, ObjectId id) {
        try (client) {  // Ensure connection is closed after use
            db = client.getDatabase("test");
            DocCollection = db.getCollection("documents");  // Not needed here
            System.out.println(DocCollection.countDocuments());
            Document document = DocCollection.find(new Document("_id", id)).first();
            if (document == null) {
                System.err.println("Document not found with ID: " + id);
                return null;
            }
            return document;
        } catch (MongoException e) {
            System.err.println("Error retrieving document: " + e.getMessage());
            return null;
        }
    }
    public static Document getLastInsertedQuery(MongoClient client) {
        try {
            // Ensure connection is closed after use
            try (client) {
                 db2 = client.getDatabase("Salma");
                MongoCollection<Document> queryCollection = db2.getCollection("query");

                // Sort documents in descending order based on the _id field
                Document lastQuery = queryCollection.find()
                        .sort(Sorts.descending("_id"))
                        .first();

                if (lastQuery == null) {
                    System.err.println("No documents found in the 'query' collection.");
                    return null;
                }
                return lastQuery;
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving last inserted query: " + e.getMessage());
            return null;
        }
    }
    public static ArrayList<String>queryProcessing(String query) throws IOException {
        ArrayList<String> datbseQuery = new ArrayList<>();
       // query = query.toLowerCase(); // Convert query to lowercase for easier stopWords removal
        ArrayList<Document> queryInfo = new ArrayList<>();
        String[] tokens = query.split("\\s+");

        for (String token : tokens) {
            try {
                token = removeStopWords(token); // Remove stop words
                if (token != null) { // Check if token is not null after stop words removal
                    token = Stemming(token); // Perform stemming
                    if (token != null) { // Check if token is not null after stemming
                        token = token.trim(); // Trim leading and trailing whitespace
                        if (!token.isEmpty()) { // Check if token is not empty after trimming
                            datbseQuery.add(token); // Add token to the list
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Tokens of the query:");
        for (String token : datbseQuery) {
            System.out.println(token);
        }

        return datbseQuery;
    }
    public static void getStopwords(String filePath) throws IOException {
        try {
            Scanner scanner = new Scanner(new File(filePath));

            while (scanner.hasNext()) {
                String word = scanner.nextLine().trim();
                stopWords.add(word);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found at " + filePath);
        }
    }
    private static String removeStopWords(String input) throws IOException {
        input = input.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").trim();
        StringBuffer stopWordResult = new StringBuffer(" ");
        String[] words = input.split("\\s+");
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                stopWordResult.append(word).append(" ");
            }
        }
        return stopWordResult.toString();
    }

    private static String Stemming(String input) throws IOException {
        PorterStemmer porterStemmerr = new PorterStemmer();
        StringBuffer stemmingResult = new StringBuffer(" ");
        String[] words = input.split("\\s+");
        for (String word : words) {
            stemmingResult.append(porterStemmerr.stem(word)).append(" ");
        }
        return stemmingResult.toString();
    }
    /////////////////////////////////////////////////////////
    private static double getInfo()
    {
        double score=0.0;
        return score;
    }
    private static HashMap<String, Object> docData(Document document) {
        HashMap<String, Object> details = new HashMap<>();
        details.put("_id", document.getObjectId("_id"));
        details.put("url", document.getString("url"));
        Integer pop = document.getInteger("popularity");
        details.put("popularly", pop);
        return details;
    }
    private static HashMap<String, Object> wordData(Document document) {
        HashMap<String, Object> details = new HashMap<>();
        details.put("_id", document.getObjectId("_id"));
        details.put("word", document.getString("word"));
        details.put("DocsCount", document.getInteger("DocsCount"));
        details.put("IDF", document.getDouble("IDF"));
        // Handle Arrayofdocs field
        List<ObjectId> docsLinks = new ArrayList<>();
        List<?> rawLinks = document.getList("ArrayOfdocs", Object.class);
        if (rawLinks != null) {
            for (Object rawLink : rawLinks) {
                if (rawLink instanceof ObjectId) {
                    ObjectId objectId = (ObjectId) rawLink;
                    docsLinks.add(objectId);
                }
            }
        }
        details.put("ArrayOfdocs", docsLinks);

        return details;
    }
    private static HashMap<String, Object> DocWordData(Document document) {
        HashMap<String, Object> details = new HashMap<>();
        details.put("_id", document.getObjectId("_id"));
        ObjectId Docid = document.getObjectId("Docid");
        details.put("Docid", Docid);
        Double tf = document.getDouble("tf");
        details.put("tf", tf);
        List<Integer> Positions = (List<Integer>) document.get("Positions");
        details.put("Positions", Positions);
        return details;
    }
    private static String extractQuery(Document document) {
       String query = document.getString("Query");
        return query;
    }


}
