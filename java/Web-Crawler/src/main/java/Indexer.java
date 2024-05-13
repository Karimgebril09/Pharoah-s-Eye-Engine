import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Indexer {
    //data structures
    private static BufferedReader reader; //read links
    public static Set<String> stopwords = new HashSet<>();
    public static ArrayList<HashMap<String, Integer>> pos_wrd_Cnt;  // word appeard in position x n time in one doc

    public static HashMap<String,Integer> WordDocsCount;  // words apeared in n docs

    public static HashMap<String, ArrayList<ObjectId>> DocsOfWord = new HashMap<String, ArrayList<ObjectId>>();// words apeared in n docs

    private static final int POSITIONS_SIZE = 7;

    private static double NumberOfPagesCounter;

    ///////  DataBase  /////


    public static Database DBhandler;
    private static int lengthOfDocument;

    private static int popularity;

    public static void main(String[] args) throws IOException {
        try {
            reader = new BufferedReader(new FileReader("src/main/java/output"));
        } catch (IOException e) {
            System.err.println("Error initializing BufferedReader: " + e.getMessage());
        }
        //readstopwords in hashset
        fillSetFromFile("src/main/java/Stopwords.txt");
        DBhandler=new Database();
        DBhandler.initDataBase();
        WordDocsCount= DBhandler.getallPreviousWords();    // initialize hashmap depending on database
        NumberOfPagesCounter=DBhandler.getInitNumberOfPages();  // return old Page numbers in variable
        String url = readNextLine();
        Document document =null;
        while (url != null ) {
            if( !DBhandler.DoesUrlExist(url)) {
                NumberOfPagesCounter++;
                lengthOfDocument = 0;
                try {
                    document = Jsoup.connect(url).get();
                }catch(Exception e) {
                    url = readNextLine();
                    pos_wrd_Cnt.clear();
                    continue;
                }
                handler(document);
                Elements paragraphs = document.select("p");

                // Extract text from paragraphs
                StringBuilder allParagraphsText = new StringBuilder();
                for (Element paragraph : paragraphs) {
                    allParagraphsText.append(paragraph.text()).append("\n");
                }

                ObjectId docid = DBhandler.insertDocument(url, Optional.of(popularity),document.body().text(),document.select("title").text(),allParagraphsText.toString());
                PassWordsToDB(docid);
                pos_wrd_Cnt.clear();
                System.out.println("finished a doc");
            }
            url = readNextLine();
        }
        insertAllWords();
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
        String[] arr = {title, high_headers, mid_headers, low_headers, body, bolds, leftovers};
        //remove stop words and stem them

        for (int i = 0; i < arr.length; i++) {
            //remove stopping words
            try {
                arr[i] = removeStopwords(arr[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                arr[i] = Stemming(arr[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                lengthOfDocument++;
                count = allWordsCount.getOrDefault(word, 0);
                allWordsCount.put(word, count + 1);
            }
        }

        // Store the counts of all words in the last position of pos_wrd_Cnt
        pos_wrd_Cnt.add(allWordsCount);

        for(int i=0;i<=POSITIONS_SIZE;i++)
        {
            pos_wrd_Cnt.get(i).remove("");
        }
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


//     ArrayOfdocs;  pos_wrd_Cnt;
    public static void PassWordsToDB(ObjectId docid) {
        ArrayList<Integer> PositionOfWord = new ArrayList<Integer>();
        String Word;
        float TF;
        ObjectId ReturnedId;
        int AllWordsSize=pos_wrd_Cnt.get(POSITIONS_SIZE).size();
        for(int i=0;i<AllWordsSize;i++)
        {
            Word=pos_wrd_Cnt.get(POSITIONS_SIZE).entrySet().iterator().next().getKey();
            TF=pos_wrd_Cnt.get(POSITIONS_SIZE).entrySet().iterator().next().getValue()/(float)lengthOfDocument;
            for(int j=0;j<POSITIONS_SIZE;j++) {
                PositionOfWord.add(pos_wrd_Cnt.get(j).getOrDefault(Word, 0));
                pos_wrd_Cnt.get(j).remove(Word);
            }
            pos_wrd_Cnt.get(POSITIONS_SIZE).remove(Word);
            ReturnedId=DBhandler.insert_DocWordData(PositionOfWord,TF,docid);
            addValueToArrayList(Word,ReturnedId);
            PositionOfWord.clear();
        }

    }

    // Retrieve the ArrayList associated with the key
    public static void addValueToArrayList(String key, ObjectId value) {
        // Retrieve the ArrayList associated with the key
        ArrayList<ObjectId> arrayList = DocsOfWord.getOrDefault(key, new ArrayList<>());
        // Add the new value to the ArrayList
        arrayList.add(value);

        // Put the updated ArrayList back into the HashMap
        DocsOfWord.put(key, arrayList);
        WordDocsCount.put(key,WordDocsCount.getOrDefault(key, 0)+1);
    }
    public static void insertAllWords(){
        String Word;
        int NumberOfDocs;
        double IDF;
        ArrayList<ObjectId> arr;
        int WordDocsSize=WordDocsCount.size();
        for(int i=0;i<WordDocsSize;i++)
        {
            Word=WordDocsCount.entrySet().iterator().next().getKey();
            NumberOfDocs=WordDocsCount.entrySet().iterator().next().getValue();
            IDF=Math.log((NumberOfPagesCounter/ (double)NumberOfDocs));  //edited since its not dependent on crawler
            arr=DocsOfWord.get(Word);
            WordDocsCount.remove(Word);
            DBhandler.Wordinsertion(Word,NumberOfDocs,IDF,arr);
        }
    }

}


