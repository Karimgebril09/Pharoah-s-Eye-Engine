/*import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

public class WebsitePopularityChecker {
    private static final String API_KEY = "YOUR_SIMILARWEB_API_KEY";

    public static void main(String[] args) throws Exception {
        String websiteUrl = "https://web.facebook.com/";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.similarweb.com/v1/website/" + websiteUrl + "/traffic-and-engagement/global-rank")
                .header("Authorization", "Bearer " + API_KEY)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        JSONObject json = new JSONObject(responseBody);
        int globalRank = json.getJSONObject("globalRank").getInt("rank");

        System.out.println("Global Rank for " + websiteUrl + ": " + globalRank);
    }
}*/
