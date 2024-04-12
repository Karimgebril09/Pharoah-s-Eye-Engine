import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Crawler {
    private static HashMap<String,Integer> visitedurls = new HashMap<>();
    private static HashMap<String,Integer> visitedurlsbody = new HashMap<>();
    private static BufferedWriter writer;

    private static final int THREAD_NUM=15;
    private static final int MAX_DEPTH = 5;//levels max
    private static final int MAX_QUEUE_SIZE = 1000;//max crawler size
    private static final String[] Seeds = {
            "https://en.wikipedia.org/wiki/Main_Page",
            "https://www.britannica.com/",
            "https://www.nasa.gov/",
            "https://www.nationalgeographic.com/",
            "https://www.history.com/",
            "https://www.scientificamerican.com/",
            "https://www.bbc.com/",
            "https://www.nytimes.com/",
            "https://www.reddit.com/",
            "https://www.youtube.com/",
            "https://www.espn.com/",
            "https://www.theguardian.com/uk/sport"
            // Add more seeds as needed
    };

    public static void main(String[] args) {
        // Initialize the queue with seed URLs
        Queue<String> UrlsQueue = new LinkedList<>(Arrays.asList(Seeds));
        Set<String> visitedUrls = new HashSet<>();
        try {
            writer = new BufferedWriter(new FileWriter("output.txt"));
        } catch (IOException e) {
            System.err.println("Error initializing BufferedWriter: " + e.getMessage());
        }

        //crawl(UrlsQueue, visitedUrls,1);

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
                for (Element link : doc.select("a[href]")) {
                    String nextLink = link.absUrl("href");
                    if (!visitedUrls.contains(nextLink) && currentDepth < MAX_DEPTH) {
                        synchronized (UrlsQueue) {
                            UrlsQueue.offer(nextLink); // Add the new URL to the end of the queue
                        }
                        crawl(UrlsQueue, visitedUrls, currentDepth + 1); // Recursive call with increased depth
                    }
                }
            }
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
            System.err.println(Thread.currentThread().getName()+" Error in Connecting to the robot.txt or in parsing: " + url + ": " + e.getMessage());
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
            System.err.println("Error in URI syntax: " + url + ": " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + url);
        } catch (IOException e) {
            System.err.println("Error connecting to " + url + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other error in request method for " + url + ": " + e.getMessage());
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
