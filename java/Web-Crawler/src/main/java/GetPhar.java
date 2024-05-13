import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class GetPhar {
    public static MongoDatabase db;
    public static MongoCollection<org.bson.Document> wordDocCollection;
    public static MongoCollection<org.bson.Document> WordDocCollection;
    public static MongoCollection<org.bson.Document> DocCollection;
    public static ArrayList<Document>AllWords=new ArrayList<>();
    public static Set<String> stopWords = new HashSet<>();
    public static void main(String[] args) throws IOException {
        String path="src/main/java/Stopwords.txt";//Step1 extracting StopWords
        getStopwords(path);

        MongoClient client = createConnection();
        collect(client);
        for (Document doc : AllWords) {
            for(ObjectId ID:(List<ObjectId>) doc.get("ArrayOfdocs"))
                    {
                        client = createConnection();
                        Document wordDoc = getWordDoc(client,ID);
                        if(wordDoc!=null)
                        {
                            client = createConnection();
                            Document docdoc=getDoc(client,wordDoc.getObjectId("Docid"));
                            if(docdoc!=null)
                            {
                               String found=findParagraph(docdoc.getString("body"), doc.getString("word"));
                               if(found!=null)
                               {
                                   client = createConnection();
                                   insertpharagrah(client,docdoc.getString("url"),doc.getString("word"),docdoc.getObjectId("_id"),found);
                                   System.out.println("******************************");
                               }
                            }
                        }
                    }
        }
    }

    public static MongoClient createConnection() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        try {
            MongoClient client = MongoClients.create(connectionString);
            return client;
        } catch (MongoException e) {
            System.out.println("Error creating connection: " + e.getMessage());
            return null;
        }
    }
    public static void collect(MongoClient client) {
        try {
            try (client) {
                db = client.getDatabase("test");
                wordDocCollection = db.getCollection("Words");
                FindIterable<Document> documents = wordDocCollection.find();

                // Iterate over the documents and add them to the list
                for (Document doc : documents) {
                    AllWords.add(doc);
                    //System.out.println(doc);
                }
                //System.out.println(AllWords.size());


            }
        } catch (MongoException e) {
            System.out.println("Error retrieving documents: " + e.getMessage());

        }
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
    public static void insertpharagrah(MongoClient client,String Url,  String word,ObjectId Id,String pharagraph){
        try {
            // Ensure connection is closed after use
            try (client) {
                db = client.getDatabase("Salma");
                MongoCollection<org.bson.Document> collection = db.getCollection("Pharagraphs");
                // Create document to insert
                org.bson.Document paragraphDoc = new org.bson.Document();
                paragraphDoc.append("url", Url);
                paragraphDoc.append("word",word);
                paragraphDoc.append("newId",Id);
                paragraphDoc.append("pharagraph",pharagraph);
                collection.insertOne(paragraphDoc);
            }
        } catch (MongoException e) {
            System.err.println("Error inserting result: " + e.getMessage());
        }

    }
    public static String findParagraph(String text,String wordd) throws IOException {
        StringBuilder result = new StringBuilder();
        // Split the text into words based on spaces

        List<String> words = new ArrayList<>();
        for (String word : text.split("\\s")) {
            words.add(word);
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
                    if (lis.contains(wordd))
                    {
                        System.out.println(lis);
                        group.clear(); // Clear the group for the next iteration
                        wordCount = 0;
                        return currentGroup.toString();
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

    private static String Stemming(String input) throws IOException {
        PorterStemmer porterStemmerr = new PorterStemmer();
        StringBuffer stemmingResult = new StringBuffer(" ");
        String[] words = input.split("\\s+");
        for (String word : words) {
            stemmingResult.append(porterStemmerr.stem(word)).append(" ");
        }
        return stemmingResult.toString();
    }

}
