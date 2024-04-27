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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mongodb.client.model.Sorts;
public class Ranker {
    public static Set<String> stopWords = new HashSet<>();
   // public static Set<String> stopWords = new HashSet<>();
    public static MongoDatabase db;
    public static MongoDatabase db2;
    public static MongoCollection<org.bson.Document> DocCollection;
    public static MongoCollection<org.bson.Document> WordsCollection;
    public static MongoCollection<org.bson.Document> WordDocCollection;
    public static MongoCollection<org.bson.Document> Query;
    public static Database DBhandler;
    public static ArrayList<HashMap<String, Object>> wordColofQuery = new ArrayList<>();
    public static ArrayList<ObjectId> resultsIds = new ArrayList<>();
    public static HashMap<ObjectId, Double> scoreshashmap = new HashMap<>();
    public static ArrayList<HashMap<String, Object>> finalResult = new ArrayList<>();
    public static ArrayList<Double> weights = new ArrayList<>(Arrays.asList(2.0, 1.0, 0.8, 0.6, 0.4, 0.2, 0.1,0.05));
    public static ArrayList<String> afterProcessing=new ArrayList<>();
    public static String query;

    public static void main(String[] args) throws IOException {
       // String query ; ///to be modifed get from react
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
            List<Integer> posy = Arrays.asList(1, 1, 0, 0, 0, 0, 0, 0);
            System.out.println(weightOfPos(posy));

            Document queryDocument = getLastInsertedQuery(client);
            query=extractQuery(queryDocument);
            System.out.println(query);
            afterProcessing = queryProcessing(query);
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
            double score=0.0;
            //HashMap<ObjectId, Double> temphash = new HashMap<>();
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
                            List<Integer> pos = (List<Integer>) temphash2.get("Positions");
                            //System.out.println((Double) wordDataMap.get("IDF")+" "+temp+" "+(Double) temphash2.get("tf"));
                            //System.out.println(pos+" "+weights+" "+weightOfPos(pos));
                            ObjectId docId = (ObjectId) temphash2.get("Docid");
                            double currentScore = scoreshashmap.getOrDefault(docId, 0.0);
                            //System.out.println(score+" "+docId+" "+weightOfPos(pos)+" "+currentScore+" "+ score+" "+weightOfPos(pos) );
                            double total=currentScore + score+weightOfPos(pos);
                            scoreshashmap.put(docId, total);
                            //System.out.println("********************"+" "+total);
                        }
                    }
                } else {
                    System.err.println("ArrayOfdocs is null in wordDataMap: " + wordDataMap);
                }
                System.out.println("passed");
            }

            // Print temphash
            Sorter(scoreshashmap);

           /* for (HashMap.Entry<ObjectId, Double> entry : scoreshashmap.entrySet()) {
                ObjectId key = entry.getKey();
                Double s=entry.getValue();
                System.out.println(key+"x"+s);
            }*/
            System.out.println("passed");
            for (ObjectId entry : resultsIds) {

                System.out.println(entry);
            }
            HashMap<String,Object>docData;
            client = createConnection();
            dropCollection(client);
            for (ObjectId temp:resultsIds) {
                client = createConnection();

                    document = getDoc(client, temp);

                    if (document != null) {
                        //System.out.println( );
                        //HashMap<String, Object> DocWordData = DocWordData(document);
                        //System.out.println(DocWordData);
                        //HashMap<String, Object> DocData = docData(document);
                        //System.out.println(DocData.get("url"));
                        docData = docData(document);
                       // System.out.println(docData);
                        finalResult.add(docData);
                        client = createConnection();
                        insertresult(docData,client);
                        System.out.println("passeslllllllll");

                    } else {
                        System.out.println("this doc is not in the indexer");
                    }}




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
    public static Double weightOfPos(List<Integer> original) {
        double additional = 0.0;
        int x = 0;
        for (Integer i : original) {
            additional += i * weights.get(x); // Access weights using get method
           // System.out.println(additional+" "+i+" "+weights.get(x));
            x++;
        }
        return additional;
    }
    public static void insertresult(HashMap<String, Object> docData, MongoClient client) {
        try {
            // Ensure connection is closed after use
            try (client) {
                db2 = client.getDatabase("Salma");
                MongoCollection<Document> collection = db2.getCollection("result");

                // Drop the collection to remove older results
               // collection.drop();

                // Create the collection again after dropping
                //collection = db2.getCollection("result");

                // Extract the first three lines of the body as the paragraph preview
                String paragraphPreview = findParagraph(joinLines((String) docData.get("body")));

                // Create document to insert
                Document paragraphDoc = new Document();
                paragraphDoc.append("title", (String) docData.get("title"));
                paragraphDoc.append("url", (String) docData.get("url"));
                paragraphDoc.append("paragraph", paragraphPreview);

                collection.insertOne(paragraphDoc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (MongoException e) {
            System.err.println("Error inserting result: " + e.getMessage());
        }
    }
    public static void dropCollection( MongoClient client) {
        try {
            // Ensure connection is closed after use
            try (client) {
                MongoDatabase database = client.getDatabase("Salma");
                database.getCollection("result").drop();
                System.out.println("Collection " + "result" + " dropped successfully.");
            } catch (Exception e) {
                System.err.println("Error dropping collection: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
        }
    }


    public static String findParagraph(String text) throws IOException {
        StringBuilder result = new StringBuilder();
        Set<String> querySet = new HashSet<>(afterProcessing);
        int wordCount = 0;
        boolean found = false;

        // Split the text into paragraphs based on empty lines
        String[] paragraphs = text.split("\\n\\s*\\n");

        // Iterate over each paragraph
        for (String paragraph : paragraphs) {
            // Split the paragraph into lines

            ArrayList<String> lines = queryProcessing(paragraph);

            // Iterate over each line in the paragraph
            for (String line : lines) {
                // Split the line into words
                String[] words = line.split("\\s+");

                // Check each word in the line
                for (String word : words) {
                    // Append the word to the result
                    result.append(word).append(" ");
                    wordCount++;

                    // If the word count exceeds 100, stop processing
                    if (wordCount >= 100) {
                        return result.toString().trim();
                    }

                    // If the current word is in the set of queries, mark the paragraph as found
                    if (querySet.contains(word.toLowerCase())) {
                        found = true;
                        break;
                    }
                }

                // If the paragraph contains one of the queries, append it to the result
                if (found) {
                    for (String l : lines) {
                        result.append(l).append("\n");
                    }
                    return result.toString().trim();
                }
            }
        }

        // If none of the queries is found, return the first 100 words
        return result.toString().trim();
    }
    public static String joinLines(String block) {
        // Split the block into lines based on full stops
        String[] lines = block.split("\\.");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line.trim()).append("\n"); // Trim leading and trailing whitespace
        }
        return result.toString();
    }

    public static Document getWordDoc(MongoClient client, ObjectId id) {
        try (client) {  // Ensure connection is closed after use
            db = client.getDatabase("test");
            WordDocCollection = db.getCollection("Word_Document");  // Not needed here
            //System.out.println(WordDocCollection.countDocuments());
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
            //System.out.println(WordDocCollection.countDocuments());
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
            //System.out.println(DocCollection.countDocuments());
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

       /* System.out.println("Tokens of the query:");
        for (String token : datbseQuery) {
            System.out.println(token);
        }*/

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
    public static void Sorter(HashMap<ObjectId, Double> map) {
        // Convert the map to a list of entries
        List<Map.Entry<ObjectId, Double>> entryList = new ArrayList<>(map.entrySet());

        // Sort the list based on values using a custom comparator
        Collections.sort(entryList, new Comparator<Map.Entry<ObjectId, Double>>() {
            @Override
            public int compare(Map.Entry<ObjectId, Double> entry1, Map.Entry<ObjectId, Double> entry2) {
                // Compare values (descending order)
                return entry2.getValue().compareTo(entry1.getValue()); // Reversed order
            }
        });

        // Clear the original map
        map.clear();

        // Put the sorted entries back into the map
        for (Map.Entry<ObjectId, Double> entry : entryList) {
            map.put(entry.getKey(), entry.getValue());
            ObjectId key = entry.getKey();
            Double s = entry.getValue();
            System.out.println(key + "  " + s);
            resultsIds.add(key);
        }
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
        details.put("body", document.getString("body"));
        Integer pop = document.getInteger("popularity");
        details.put("popularly", pop);
        details.put("p", document.getString("p"));
        details.put("title", document.getString("title"));
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
