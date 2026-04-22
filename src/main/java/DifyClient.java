import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class DifyClient {

    private final String apiKey;
    private final String datasetId;
    private static final String BASE_URL = "https://api.dify.ai/v1";

    public DifyClient(String apiKey, String datasetId) {
        this.apiKey    = apiKey;
        this.datasetId = datasetId;
    }

    public void addWebDocument(String url) throws Exception {
        String body = """
                {
                  "indexing_technique": "high_quality",
                  "data_source": {
                    "type": "web_page",
                    "info_list": {
                      "data_source_type": "web_page",
                      "web_page_list": ["%s"]
                    }
                  },
                  "process_rule": { "mode": "automatic" }
                }
                """.formatted(url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/datasets/" + datasetId + "/document/create-by-url"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Dify API fejl: " + response.statusCode() + " – " + response.body());
        }
    }
}
