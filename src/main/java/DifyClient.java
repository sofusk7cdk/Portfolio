import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class DifyClient {

    private final String apiKey;
    private final String datasetId;
    private final String jinaApiKey;
    private static final String DIFY_BASE_URL = "https://api.dify.ai/v1";
    private static final String JINA_BASE_URL = "https://r.jina.ai/";

    public DifyClient(String apiKey, String datasetId, String jinaApiKey) {
        this.apiKey     = apiKey;
        this.datasetId  = datasetId;
        this.jinaApiKey = jinaApiKey;
    }

    public void addWebDocument(String url) throws Exception {
        String content = fetchWithJina(url);

        String body = """
                {
                  "name": "%s",
                  "text": %s,
                  "indexing_technique": "high_quality",
                  "process_rule": { "mode": "automatic" }
                }
                """.formatted(url, toJsonString(content));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DIFY_BASE_URL + "/datasets/" + datasetId + "/document/create-by-text"))
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

    private String fetchWithJina(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(JINA_BASE_URL + url))
                .header("Authorization", "Bearer " + jinaApiKey)
                .header("Accept", "text/plain")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Jina fejl: " + response.statusCode());
        }

        return response.body();
    }

    private String toJsonString(String text) {
        return "\"" + text.replace("\\", "\\\\")
                          .replace("\"", "\\\"")
                          .replace("\n", "\\n")
                          .replace("\r", "\\r")
                          .replace("\t", "\\t") + "\"";
    }
}
