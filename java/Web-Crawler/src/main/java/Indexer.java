import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import java.util.Set;
import java.util.HashSet;
public class Indexer {
    //data structures
    private static BufferedReader reader; //read links
    public  static Set<String> stopwords = new HashSet<>();
    public static ArrayList<HashMap<String,Integer>> pos_wrd_Cnt;

    private static final int POSITIONS_SIZE = 7;

    enum tags {
        title,high_headers,mid_headers,low_headers,body,bolds,leftovers
    };
/*
* To DO:
* 0-sequence of reading
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
            document = Jsoup.connect(url).get();
            handler(document);


            url= readNextLine();
        }
        closeReader();
    }

    public static void handler(Document document) {
        //priority: title> high_headers> mid_headers> low_headers> body> bolds> leftovers
        String title = document.select("title").text();
        String high_headers = document.select("h1,h2").text();
        String mid_headers = document.select("h3,h4").text();
        String low_headers = document.select("h5,h6").text();
        String body = document.select("p,span,code,textarea").text();
        String bolds = document.select("b,strong,i,em,blockquote").text();
        // Selecting leftovers: all text nodes not within tags covered above
        String leftovers = document.select("a,ul, ol, li, table, tr, td, th, form").text();
        String[] arr={title,high_headers,mid_headers,low_headers,body,bolds,leftovers};
        //remove stop words and stem them

        for(int i=0;i< arr.length;i++)
        {
            //remove stopping words
            try {
                arr[i]=removeStopwords(arr[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                arr[i]=Stemming(arr[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(i+ " "+ arr[i]);
        }
        filler(arr);
    }
    public static void filler(String[] arr) {
        pos_wrd_Cnt = new ArrayList<HashMap<String, Integer>>();

        // Initialize a HashMap to store counts of all words
        HashMap<String, Integer> allWordsCount = new HashMap<>();

        // Loop through each position
        for (int i = 0; i < POSITIONS_SIZE; i++) {
            // Initialize a new HashMap for the current position
            HashMap<String, Integer> wordCountMap = new HashMap<>();
            pos_wrd_Cnt.add(wordCountMap);

            // Split the input string into words
            String[] words = arr[i].split("\\s+");

            // Update counts for words in the current position
            for (String word : words) {
                // Update count for the current position
                int count = wordCountMap.getOrDefault(word, 0);
                wordCountMap.put(word, count + 1);

                // Update count for all words
                count = allWordsCount.getOrDefault(word, 0);
                allWordsCount.put(word, count + 1);
            }
        }

        // Store the counts of all words in the last position of pos_wrd_Cnt
        pos_wrd_Cnt.add(allWordsCount);
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
        input = input.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").trim();
        StringBuffer result = new StringBuffer(" ");
        String[] words = input.split("\\s+");
        for (String word : words) {
            if (!stopwords.contains(word.toLowerCase())) {
                result.append(word).append(" ");
            }
        }
        return result.toString();
    }
    public static String Stemming(String input) throws IOException {
        PorterStemmer porterStemmer = new PorterStemmer();
        StringBuffer result = new StringBuffer(" ");
        String[] words = input.split("\\s+");
        for (String word : words) {
            result.append(porterStemmer.stem(word)).append(" ");
        }
        return result.toString();
    }
}
