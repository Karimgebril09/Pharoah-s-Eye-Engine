import org.bson.conversions.Bson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.*;
import java.io.IOException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.reflect.Array.set;

public class Crawler {
    //private static HashMap<String,Integer> visitedurls = new HashMap<>();
    private static HashMap<String,Integer> visitedurlsbody = new HashMap<>();
    private static BufferedWriter writer;
    private static final int THREAD_NUM=15;
    private static final int MAX_DEPTH = 1;//levels max
    private static final int MAX_QUEUE_SIZE = 7000;//max crawler size
    public static MongoDatabase db;
    public static MongoCollection<org.bson.Document> DocRankCollection;
    public static void main(String[] args) {
        List<String> Seeds = readSeedsFromFile("src/main/java/Seed.txt");
        Queue<String> UrlsQueue = new LinkedList<>(Seeds);
        Set<String> visitedUrls = new HashSet<>();
        try {
            writer = new BufferedWriter(new FileWriter("src/main/java/output2"));
        } catch (IOException e) {
            System.err.println("Error initializing BufferedWriter: " + e.getMessage());
        }

        Thread[] crawlerThreads=new Thread[THREAD_NUM];
        for(int i=0;i<THREAD_NUM;i++){
            crawlerThreads[i]=new Thread(new RunnableCrawler(UrlsQueue,visitedUrls));
            crawlerThreads[i].start();
        }

        for(int i=0;i<THREAD_NUM;i++){
            try {
                crawlerThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        closeWriter(); // close the writer
    }

    private static class RunnableCrawler implements Runnable{
        private Queue<String> UrlsQueue;
        private Set<String> visitedUrls;


        public RunnableCrawler(Queue<String> UrlsQueue ,  Set<String> visitedUrls){
            this.UrlsQueue=UrlsQueue;
            this.visitedUrls=visitedUrls;
        }

        public void run(){
            crawl(UrlsQueue,visitedUrls,1);
        }
    }
    //private static HashMap<String,Integer> visitedurlsbody = new HashMap<>();

    private static void crawl(Queue<String> UrlsQueue, Set<String> visitedUrls, int currentDepth) {
        while (!UrlsQueue.isEmpty()) {
            if (visitedUrls.size() >= MAX_QUEUE_SIZE) {
                System.out.println("Maximum queue size reached. Stopping crawler.");
                break;
            }
            String currentUrl;
            synchronized (UrlsQueue) { //synchronize shared object to avoid corruption
                currentUrl = UrlsQueue.poll();
                if (visitedUrls.contains(currentUrl)) {
                    continue; // Skip if the seed has already been visited
                }
            }
            Document doc = request(currentUrl, visitedUrls);
            if (doc != null) {
                Set<String> outgoingLinks = new HashSet<>();
                for (Element link : doc.select("a[href]")) {
                    String nextLink = link.absUrl("href");
                    outgoingLinks.add(nextLink);
                    if (!visitedUrls.contains(nextLink) && currentDepth < MAX_DEPTH) {
                        synchronized (UrlsQueue) {
                            UrlsQueue.offer(nextLink); // Add the new URL to the end of the queue
                        }
                        crawl(UrlsQueue, visitedUrls, currentDepth + 1); // Recursive call with increased depth
                    }
                }
                MongoClient client=createConnection();
                insertOutgoingLinksIntoDatabase(client,currentUrl, outgoingLinks);
            }
        }
    }
    public static void insertOutgoingLinksIntoDatabase(MongoClient client, String currentUrl, Set<String> outgoingLinks) {
        try {
            try (client) {
                db = client.getDatabase("Salma");
                MongoCollection<org.bson.Document> collection = db.getCollection("Rankerr");

                // Check if document with currentUrl already exists
                org.bson.Document existingDoc = collection.find(eq("url", currentUrl)).first();
                if (existingDoc != null) {
                    // Document with currentUrl already exists, update outgoings field
                   /* ArrayList<String> existingOutgoings = existingDoc.get("outgoings", ArrayList.class);
                    if (existingOutgoings == null) {
                        existingOutgoings = new ArrayList<>();
                    }
                    for (String link : outgoingLinks) {
                        existingOutgoings.add(link);
                    }
                    collection.updateOne(eq("url", currentUrl), new Document("$set", new Document("outgoings", existingOutgoings)));*/

                    return;

                } else {
                    ArrayList<Document> outgoingLinksList = new ArrayList<>();
                    for (String link : outgoingLinks) {
                        outgoingLinksList.add(new Document("link", link));
                    }
                    org.bson.Document paragraphDoc = new org.bson.Document();
                    paragraphDoc.append("url", currentUrl);
                    paragraphDoc.append("outgoings",outgoingLinks);
                    collection.insertOne(paragraphDoc);
                }
            }
        } catch (MongoException e) {
            System.out.println("Error inserting/updating result: " + e.getMessage());
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

    private static boolean isUrlAllowed(String url) { //Regarding the robot.txt part
        try {
            // Parse the URL to get the domain
            URI uri = new URI(url);
            String domain = uri.getHost();
            // Fetch and parse the robots.txt file
            Document robotsTxt = Jsoup.connect("http://" + domain + "/robots.txt").get();
            String robotsTxtContent = robotsTxt.text();
            // Check if the URL is allowed based on the rules in robots.txt
            return !robotsTxtContent.contains(Thread.currentThread().getName()+" Disallow: " + uri.getPath());
        } catch (Exception e) {
            // Handle exceptions
            System.out.println(Thread.currentThread().getName()+" Error in Connecting to the robot.txt or in parsing: " + url + ": " + e.getMessage());
            return false; // Assume the URL is not allowed in case of errors
        }
    }

    private static Document request(String url, Set<String> visitedUrls) {
        try {
            // Check if the URL is allowed based on robots.txt rules
            if (!isUrlAllowed(url)) {
                System.out.println("URL not allowed by robots.txt: " + url);
                return null;
            }
            // Normalize the URL using URI
            URI uri = new URI(url).normalize();
            String compactUrl = uri.toURL().toExternalForm();

            Connection con = Jsoup.connect(compactUrl);
            con.followRedirects(true); // Enable following redirects
            con.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"); // Set user-agent to mimic a browser

            // Check if the content type is HTML to satisfy the part of only html docs
            String contentType = con.execute().contentType();
            if (contentType != null && contentType.contains("text/html")) {
                Document doc = con.get();
                if (con.response().statusCode() == 200 && check_if_page_Exists(doc)) {
                    System.out.println(Thread.currentThread().getName()+" Link: " + compactUrl);
                    System.out.println(doc.title());
                    synchronized (visitedUrls) {
                        writeStringToFile(url);
                        visitedUrls.add(compactUrl);
                    }
                    return doc;
                }

            }
            return null;
        } catch (URISyntaxException e) {
            System.out.println("Error in URI syntax: " + url + ": " + e.getMessage());
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + url);
        } catch (IOException e) {
            System.out.println("Error connecting to " + url + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Other error in request method for " + url + ": " + e.getMessage());
        }
        return null;
    }
    private static boolean check_domain(String url)
    {
        return url.contains(".com") || url.contains(".edu") || url.contains(".net") || url.contains(".gov") || url.contains(".org");
    }

    private static boolean check_if_page_Exists(Document doc) {
        String shaValue = getSHA(doc);
        boolean result = visitedurlsbody.containsKey(shaValue);
        if (!result) {
            // Add SHA value to the map when page is visited
            visitedurlsbody.put(shaValue, Integer.valueOf(1)); // Assuming visitedurlsbody is a Map<String, Document>
        }
        else {
            System.out.println("the web page already existed");
        }
        return !result;
    }
    private static List<String> readSeedsFromFile(String filePath) {
        List<String> seeds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                seeds.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading seeds file: " + e.getMessage());
        }
        return seeds;
    }
    private static String getSHA(Document doc) {
        String htmlContent = doc.html();
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Update the digest with the input string bytes
            digest.update(htmlContent.getBytes());

            // Compute the hash
            byte[] hashedBytes = digest.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Handle NoSuchAlgorithmException
            e.printStackTrace();
        }
        return "";
    }

    // used to extern the links

    public static void writeStringToFile(String str) {
        try {
            writer.write(str);
            writer.newLine() ; // Writing the string on a new line
            System.out.println("String has been written to the file successfully.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void closeWriter() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing BufferedWriter: " + e.getMessage());
        }
    }
    public static int getMaxQueueSize() {
        return MAX_QUEUE_SIZE;
    }
    }
