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
