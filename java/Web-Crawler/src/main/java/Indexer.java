import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import java.util.Set;
//import org.apache.lucene.analysis.en.PorterStemmer;
import java.util.HashSet;
public class Indexer {
    //data structures
    private static BufferedReader reader; //read links
    public static HashMap<String, Integer> body_count = new HashMap<>();
    public  static Set<String> stopwords = new HashSet<>();

    enum tags {
        title,high_headers,mid_headers,low_headers,body,bolds,leftovers
    };

/*
* To DO:
* 0-sequence of reading
* 1-dont forget stemmer
* 2-calculations
* 3-insertions in db
* 4-
* */
    public static void main(String[] args) throws IOException {
        try {
            reader = new BufferedReader(new FileReader("output.txt"));
        } catch (IOException e) {
            System.err.println("Error initializing BufferedReader: " + e.getMessage());
        }
        //readstopwords in hashset
        fillSetFromFile("D:\\Pharoah_eye_project\\Pharoah-s-Eye-Engine\\java\\Web-Crawler\\src\\main\\java\\Stopwords.txt");

        String url= readNextLine();
        Document document;
        while (url != null) {
            // implement here the logic
            /*
            * 1-read title
            * 2-read headers 6 prioties
            *
            * 3-read body
            * 4-add to data structure
            * 5-
            * */
            document = Jsoup.connect(url).get();
            extractor(document);
            url= readNextLine();
        }
        closeReader();
    }

    public static void extractor(Document document) {
        //priority: title> high_headers> mid_headers> low_headers> body> bolds> leftovers
        String title = document.select("title").text();
        String high_headers = document.select("h1,h2").text();
        String mid_headers = document.select("h3,h4").text();
        String low_headers = document.select("h5,h6").text();
        String body = document.select("p,span,code").text();
        String bolds = document.select("b,strong,i,em").text();
        // Selecting leftovers: all text nodes not within tags covered above
        String leftovers = "";

        //stemming



        // Do something with extracted data
        System.out.println("Title: " + title);
        System.out.println("High Headers: " + high_headers);
        System.out.println("Mid Headers: " + mid_headers);
        System.out.println("Low Headers: " + low_headers);
        System.out.println("Body: " + body);
        System.out.println("Bolds: " + bolds);
        System.out.println("Leftovers: " + leftovers);
        int x=5;
    }

    public static String readNextLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
            return null;
        }
    }

    public static void closeReader() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing BufferedReader: " + e.getMessage());
        }
    }
    public static void fillSetFromFile(String filename) throws IOException {
        BufferedReader stopreader = new BufferedReader(new FileReader(filename));
        String line;

        // Read each line from the file and add it to the set
        while ((line = stopreader.readLine()) != null) {
            stopwords.add(line);
        }

        stopreader.close();
    }


    public static String removeStopwords(String input) throws IOException {
        input = input.replaceAll("[^a-zA-Z]", " ").replaceAll("\\s+", " ").trim();
        StringBuffer result = new StringBuffer(" ");
        String[] words = input.split("\\s+");
        for (String word : words) {
            if (!stopwords.contains(word)) {
                result.append(word).append(" ");
            }
        }
        return result.toString();
    }
}
