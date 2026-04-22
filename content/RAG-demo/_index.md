---
title: "RAG-demo: Automatiseret opdatering af Dify-bot"
description: "Dokumentation af et automatiseret Java-workflow der holder en Dify RAG-bot synkroniseret med portfolioens indhold via sitemap-parsing."
---

# RAG-demo: Automatiseret opdatering af Dify-bot

Dette projekt dokumenterer, hvordan jeg har bygget et automatiseret workflow i Java, der sørger for at min RAG-bot på Dify altid afspejler det nyeste indhold på min portfolio.

## Problemet

Når man bygger en RAG-bot med en videnbase, opstår der hurtigt et vedligeholdelsesproblem: **indholdet bliver forældet**. Hver gang jeg tilføjer en ny side til min portfolio, skal jeg manuelt opdatere Dify's knowledge base – det er upraktisk og fejlbehæftet.

Løsningen er at automatisere processen, så botten selv holder sig opdateret.

## Arkitektur

Workflowet fungerer i tre trin:

1. **Sitemap-parsing** – Java-programmet henter og parser portfolioens `sitemap.xml` for at finde alle aktive URL'er.
2. **Sammenligning** – De fundne URL'er sammenlignes med den liste, der sidst blev indekseret i Dify.
3. **Opdatering via API** – Nye URL'er sendes til Dify's knowledge base API, så indholdet crawles og indekseres.

## Java-implementering

Projektet er skrevet i **IntelliJ IDEA** og bruger en dedikeret `Sitemap`-klasse til at håndtere al XML-parsing.

### Sitemap-klassen

```java
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Sitemap {

    private final String sitemapUrl;

    public Sitemap(String sitemapUrl) {
        this.sitemapUrl = sitemapUrl;
    }

    public List<String> fetchUrls() throws Exception {
        List<String> urls = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new URL(sitemapUrl).openStream());

        NodeList locNodes = doc.getElementsByTagName("loc");
        for (int i = 0; i < locNodes.getLength(); i++) {
            urls.add(locNodes.item(i).getTextContent().trim());
        }

        return urls;
    }
}
```

`Sitemap`-klassen åbner en forbindelse direkte til sitemappets URL og parser XML-dokumentet med Java's indbyggede `DocumentBuilder`. Alle `<loc>`-tags udtrækkes og returneres som en liste af strings.

### Hoved-workflowet

```java
import java.util.List;

public class RagUpdater {

    public static void main(String[] args) throws Exception {
        String sitemapUrl = "https://sofusk7cdk.github.io/Portfolio/sitemap.xml";
        String difyApiKey = System.getenv("DIFY_API_KEY");
        String datasetId  = System.getenv("DIFY_DATASET_ID");

        Sitemap sitemap = new Sitemap(sitemapUrl);
        List<String> currentUrls = sitemap.fetchUrls();

        KnownUrlStore store = new KnownUrlStore("known_urls.txt");
        List<String> knownUrls  = store.load();
        List<String> newUrls    = currentUrls.stream()
                .filter(url -> !knownUrls.contains(url))
                .toList();

        if (newUrls.isEmpty()) {
            System.out.println("Ingen nye sider fundet – Dify er allerede opdateret.");
            return;
        }

        DifyClient dify = new DifyClient(difyApiKey, datasetId);
        for (String url : newUrls) {
            dify.addWebDocument(url);
            System.out.println("Tilføjet til Dify: " + url);
        }

        store.save(currentUrls);
        System.out.println("Færdig – " + newUrls.size() + " ny(e) side(r) indekseret.");
    }
}
```

Programmet holder styr på kendte URL'er i en lokal tekstfil (`known_urls.txt`). Kun URL'er der er nye siden sidst, sendes til Dify – det minimerer unødvendige API-kald.

### Dify API-kaldet

```java
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
```

API-nøgler og dataset-ID hentes fra miljøvariable (`DIFY_API_KEY`, `DIFY_DATASET_ID`) for at undgå at hardcode følsomme oplysninger i koden.

## Automatisering

Programmet er sat op til at køre som et **GitHub Actions workflow**, der udløses automatisk, hver gang der pushes til `main`-branchen på portfoliorepoet. Det betyder, at Dify-botten opdateres inden for minutter efter at en ny side er gået live.

```yaml
name: Sync RAG bot

on:
  push:
    branches: [main]

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Kør RAG-opdatering
        env:
          DIFY_API_KEY: ${{ secrets.DIFY_API_KEY }}
          DIFY_DATASET_ID: ${{ secrets.DIFY_DATASET_ID }}
        run: mvn -q compile exec:java -Dexec.mainClass="RagUpdater"
```

## Resultat

Botten har nu altid et opdateret billede af portfolioens indhold. Når en besøgende stiller spørgsmål om mine projekter, kan Dify hente præcis og aktuel information frem – uden at jeg behøver at løfte en finger efter et nyt deploy.
