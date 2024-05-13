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
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mongodb.client.model.Sorts;
public class Ranker3 {
    public static Set<String> stopWords = new HashSet<>();
    private static final int THREAD_NUM=10;
    public static MongoDatabase db;
    public static MongoDatabase db2;
    public static MongoCollection<org.bson.Document> DocCollection;
    public static MongoCollection<org.bson.Document> WordsCollection;
    public static MongoCollection<org.bson.Document> WordDocCollection;
    public static ArrayList<HashMap<String, Object>> wordColofQuery = new ArrayList<>();
    public static ArrayList<ObjectId> resultsIds = new ArrayList<>();
    public static HashMap<ObjectId, Double> scoreshashmap = new HashMap<>();
    public static ArrayList<HashMap<String, Object>> finalResult = new ArrayList<>();
    public static ArrayList<Double> weights = new ArrayList<>(Arrays.asList(2.0, 1.0, 0.8, 0.6, 0.4, 0.2, 0.1,0.05));
    public static ArrayList<String> afterProcessing=new ArrayList<>();
    public static String query;
    public static List<String> temp = new ArrayList<>();
    public static  List<String> tempWithoutQuotes = new ArrayList<>();
    public static Boolean needFiller;
    public static MongoClient client;
    public static  ArrayList<ObjectId> arrayOfDocs;
    public static void main(String[] args) throws IOException {
        // Capture the current time after running the code
        long startTime = System.currentTimeMillis();

        String path="src/main/java/Stopwords.txt";//Step1 extracting StopWords
        try {
            getStopwords(path);
            //////////////////////////////////////////////tested before calling the database
           client = createConnection();
            if (client == null) {
                System.err.println("Failed to create connection");
                return;
            }
            List<Integer> posy = Arrays.asList(1, 1, 0, 0, 0, 0, 0, 0);
            System.out.println(weightOfPos(posy));
            Document queryDocument = getLastInsertedQuery(client);
            query=extractQuery(queryDocument);
            System.out.println(query);
            needFiller =check(query);
            System.out.println(needFiller);
            afterProcessing = queryProcessing(query);
            System.out.println(afterProcessing);
            System.out.println("passed");
            //////////////////////////
            client = createConnection();
            updatePopularity(client);
            /////////////////////////////////
            Document document;
            HashMap<String, Object> wordData;
            for (String token : afterProcessing) {
                client = createConnection();
                if(token !=null){
                    document = getWord(client, token);
                    if (document != null) {
                        wordData = wordData(document);
                        System.out.println(wordData.get("_id"));
                        wordColofQuery.add(wordData);
                    } else {
                        System.out.println("this word is not in the indexer");
                    }}
            }
           /* double score=0.0;
            HashMap<String, Object> temphash2;
            for (HashMap<String, Object> wordDataMap : wordColofQuery) {
                List<ObjectId> arrayOfDocs = (List<ObjectId>) wordDataMap.get("ArrayOfdocs");
                if (arrayOfDocs != null) {
                    for (ObjectId temp : arrayOfDocs) {
                        client = createConnection();
                        Document doc = getWordDoc(client, temp);
                        if (doc != null) {
                            temphash2 = DocWordData(doc);
                            ObjectId docId = (ObjectId) temphash2.get("Docid");
                            client = createConnection();
                            Document doc2 = getDoc(client, docId);
                            if(doc2!= null){
                                HashMap<String, Object> temphash3 = docData(doc2);
                                score = (Double) wordDataMap.get("IDF") * (Double) temphash2.get("tf")+(Double) temphash3.get("popularity");
                                List<Integer> pos = (List<Integer>) temphash2.get("Positions");
                                double currentScore = scoreshashmap.getOrDefault(docId, 0.0);
                                double total=currentScore + score+weightOfPos(pos);
                                scoreshashmap.put(docId, total);}
                            // System.out.println("********************"+" "+total);
                        }
                    }
                } else {
                    //   System.err.println("ArrayOfdocs is null in wordDataMap: " + wordDataMap);
                }*/
                // System.out.println("passed");
            Thread[] rankerThreads=new Thread[THREAD_NUM];
            for(int i=0;i<THREAD_NUM;i++){
                rankerThreads[i]=new Thread(new Ranker3.RunnableRanker(arrayOfDocs));
                rankerThreads[i].start();
            }
            for(int i=0;i<THREAD_NUM;i++){
                try {
                    rankerThreads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long sorts=System.currentTimeMillis();
            Sorter(scoreshashmap);
            long sorte=System.currentTimeMillis();
            long secstart=System.currentTimeMillis();
            HashMap<String,Object>docData;
            client = createConnection();
            dropCollection(client);
            for (ObjectId temp:resultsIds) {
                client = createConnection();
                document = getDoc(client, temp);
                if (document != null) {
                    docData = docData(document);
                    finalResult.add(docData);
                    client = createConnection();
                    insertresult(docData,client);

                } else {
                    System.out.println("this doc is not in the indexer");
                }}
            // Capture the current time after running the code
            long secend=System.currentTimeMillis();
            long endTime = System.currentTimeMillis();

            // Calculate the time taken by subtracting start time from end time
            long executionTime = endTime - startTime;
            long sortt=sorte-sorts;
            long pro=secend-secstart;

            // Print the execution time
            System.out.println("Time taken: " + executionTime + " milliseconds");
            System.out.println("sort taken: " + sortt + " milliseconds");
            System.out.println("pro taken: " + pro + " milliseconds");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static class RunnableRanker implements Runnable{
        ArrayList<ObjectId>listid;
        MongoClient client;



        public RunnableRanker(ArrayList<ObjectId>listids){
            this.listid=listids;

        }

        public void run(){
            Calculate();
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
    public static void Calculate() {
        double score = 0.0;
        HashMap<String, Object> temphash2;
        for (HashMap<String, Object> wordDataMap : wordColofQuery) {
            arrayOfDocs = (ArrayList<ObjectId>) wordDataMap.get("ArrayOfdocs");
            if (arrayOfDocs != null) {
                ObjectId temp;
                while (!arrayOfDocs.isEmpty()) {
                    synchronized (arrayOfDocs) {
                        temp = arrayOfDocs.removeFirst();
                    }
                    client = createConnection();
                    Document doc = getWordDoc(client, temp);
                    if (doc != null) {
                        temphash2 = DocWordData(doc);
                        ObjectId docId = (ObjectId) temphash2.get("Docid");
                        client = createConnection();
                        Document doc2 = getDoc(client, docId);
                        if (doc2 != null) {
                            HashMap<String, Object> temphash3 = docData(doc2);
                            score = (Double) wordDataMap.get("IDF") * (Double) temphash2.get("tf") + (Double) temphash3.get("popularity");
                            List<Integer> pos = (List<Integer>) temphash2.get("Positions");
                            double currentScore = scoreshashmap.getOrDefault(docId, 0.0);
                            double total = currentScore + score + weightOfPos(pos);
                            scoreshashmap.put(docId, total);
                        }
                        // System.out.println("********************"+" "+total);
                    }
                }
            }
        }
    }
    public static Double weightOfPos(List<Integer> original) {
        double additional = 0.0;
        int x = 0;
        for (Integer i : original) {
            additional += i * weights.get(x); // Access weights using get method
            x++;
        }
        return additional;
    }
    public static Boolean check(String text) {
        List<String> words = new ArrayList<>();

        Pattern pattern = Pattern.compile("\"[^\"]+\"");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            words.add(word);
        }

        System.out.println("llllllllll");
        System.out.println(words);
        //System.out.println(words.contains("\"water\""));
        boolean foundQuotes = false;
        if(!words.isEmpty()) {
            for (String phrase : words) {
                String phraseWithoutQuotes = phrase.replaceAll("\"", "");
                tempWithoutQuotes.add(phraseWithoutQuotes);
            }
            System.out.println(tempWithoutQuotes);
            return true;
        }
        // String h=words.get(2);
        //System.out.println(h);
       /* for (String word : words) {
            if (word.startsWith("\"") && word.endsWith("\"")) {
                foundQuotes = true;
                temp.add(word);
                System.out.println("**********************************");
                System.out.println(temp);
                System.out.println(temp.contains(word));
                System.out.println("**********************************");
                //break;
            }
        }*/

        return foundQuotes;
    }
    public static void insertresult(HashMap<String, Object> docData, MongoClient client) {
        try {
            // Ensure connection is closed after use
            try (client) {
                db2 = client.getDatabase("Salma");
                MongoCollection<Document> collection = db2.getCollection("result");
                // Extract the first three lines of the body as the paragraph preview
                String paragraphPreview = findParagraph(joinLines((String) docData.get("body")));
                if(paragraphPreview==null)

                    return;
                // System.out.println(paragraphPreview);
                //System.out.println("******************************************");
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
        // Split the text into words based on spaces


        List<String> words = new ArrayList<>();
        for (String word : text.split("\\s")) {
            words.add(word);
        }
        //System.out.println(words);
        Boolean keep=false;
        Pattern pattern;
        Matcher matcher;
        if(needFiller)
        {
            for (String phrase : tempWithoutQuotes) {
                System.out.println("insideeeee");
                pattern = Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b");
                matcher = pattern.matcher(text);
                if (matcher.find()) {
                    keep = true;
                    break;
                }
            }
            if(!keep)
                return null;
        }

        // Iterate over each word
        int wordCount = 0;

        StringBuilder currentGroup = new StringBuilder();
        List<String> group = new ArrayList<>();
        for (String word : words) {
            // Add the word to the current group
            group.add(word);
            wordCount++;

            currentGroup.append(word).append(" ");

            // If the current group reaches 50 words or all words are processed
            if (wordCount == 50 || wordCount == words.size()) {
                // Check if any word in the group is in the set of queries
                ArrayList<String> lis= queryProcessing(currentGroup.toString());

                for(String word2 : afterProcessing) {
                    if (lis.contains(word2))
                    {
                        System.out.println(lis);
                        System.out.println(word2);
                        System.out.println("******************");
                        group.clear(); // Clear the group for the next iteration
                        wordCount = 0;
                        return currentGroup.toString();
                    }

                }
                group.clear(); // Clear the group for the next iteration
                wordCount = 0;
                currentGroup.setLength(0);

            }

        }
        // If none of the query words is found in any group, return null
        if (result.length() == 0) {
            return null;
        }
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
                System.out.println("Document not found with this word: " + word);
                return null;
            }
            System.out.println("Documents were found with word: " + word);
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
    public static String processSingleWord(String word) {
        String processedWord = null;

        try {
            word = removeStopWords(word); // Remove stop words
            if (word != null) { // Check if word is not null after stop words removal
                word = Stemming(word); // Perform stemming
                if (word != null) { // Check if word is not null after stemming
                    word = word.trim(); // Trim leading and trailing whitespace
                    if (!word.isEmpty()) { // Check if word is not empty after trimming
                        processedWord = word; // Assign processed word
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return processedWord;
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
        details.put("p", document.getString("p"));
        details.put("title", document.getString("title"));
        if (document.containsKey("popularity")) {
            Double pop = document.getDouble("popularity");
            details.put("popularity", pop != null ? pop : 0.0); // Set default value if null

        } else {
            details.put("popularity", 0.0); // Set default value if 'popularity' field is missing
        }
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
    public static void updatePopularity(MongoClient client ) {
        try {
            db = client.getDatabase("test");
            MongoCollection<Document> collection = db.getCollection("documents");

            UpdateResult result = collection.updateMany(
                    new Document("popularity", 0.01),
                    new Document("$set", new Document("popularity", 0.0000000000000000000000000000001))
            );
            // System.out.println(result);
            //System.out.println(result.getModifiedCount() + " documents updated.");
        } catch (MongoException e) {
            System.err.println("Error updating popularity values: " + e.getMessage());
        }
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
