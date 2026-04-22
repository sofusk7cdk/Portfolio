import java.util.List;

public class RagUpdater {

    public static void main(String[] args) throws Exception {
        String sitemapUrl = "https://sofusk7cdk.github.io/Portfolio/sitemap.xml";
        String difyApiKey = System.getenv("DIFY_API_KEY");
        String datasetId  = System.getenv("DIFY_DATASET_ID");

        Sitemap sitemap = new Sitemap(sitemapUrl);
        List<String> currentUrls = sitemap.fetchUrls();

        KnownUrlStore store = new KnownUrlStore("known_urls.txt");
        List<String> knownUrls = store.load();
        List<String> newUrls   = currentUrls.stream()
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
