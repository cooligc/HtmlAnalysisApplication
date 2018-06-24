package com.skc.scout24;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class HtmlParsing {

    public static final String HTTPS = "https";
    public static final String PROTOCOL_HTTPS = "https://";
    public static final String PROTOCOL_HTTP = "http://";
    private String url;

    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getUrl() {
        //URL has to be set
        assert url != null;
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Document getHtmlDocument() throws IOException{
        return Jsoup.connect(getUrl()).get();
    }

    public Document getHtmlDocument(String url) throws IOException{
        return Jsoup.connect(url).get();
    }

    public String getHtmlVersion(Document document) throws IOException {
        document = validateDocument(document);
        List<Node> nodes = document.childNodes();
        return getHtmlVersion(nodes);
    }

    private Document validateDocument(Document document) throws IOException{
        if(null == document){
            document = getHtmlDocument();
        }
        return document;
    }

    private String getHtmlVersion(List<Node> nodes) {
        StringBuilder stringBuilder = new StringBuilder();
        Optional<Node> _node = nodes.parallelStream().filter(node -> node instanceof DocumentType).findFirst();
        DocumentType documentType = (DocumentType) _node.get();
        //Version docs retrieve from https://www.w3schools.com/tags/tag_doctype.asp
        final Pattern pattern = Pattern.compile("([0-9]*[.])+");
        final Matcher matcher = pattern.matcher(documentType.outerHtml());
        if(!matcher.find()){
            return "5";
        }
        return matcher.group(1).substring(0,1);
    }

    public String getPageTitle(Document document) throws IOException{
        document = validateDocument(document);
        return document.title();
    }

    public List<Heading> getHeadings(final Document document) throws IOException{
        assert document != null;
        List<Heading> headingList = new ArrayList<>();
        Arrays.asList("h1","h2","h3","h4","h5").parallelStream().forEach(tag -> {
            List<Element> elements = document.getElementsByTag(tag);
            //TODO Need to optimize
            elements.parallelStream().forEach(element -> {
                Heading heading = new Heading();
                heading.setType(tag);
                heading.setValue(element.ownText());
                headingList.add(heading);
            });
        });
        return headingList;
    }

    public Map<String,Long> getHeadingCount(List<Heading> headings){
        Map<String,Long> headingCount = new HashMap<>();
        Arrays.asList("h1","h2","h3","h4","h5").parallelStream().forEach(tag -> {
            long count = headings.parallelStream().filter(heading -> heading.getType().equalsIgnoreCase(tag)).count();
            headingCount.put(tag,count);
        });
        return headingCount;
    }

    public List<Link> getAllLinks(Document htmlDocument) {
        List<Link> links = new ArrayList<>();
        List<Element> elements = htmlDocument.getElementsByTag("a");
        elements.parallelStream().forEach(element -> {
            Link link = new Link();
            link.setHref(element.attr("href"));
            String type = link.getHref().contains(getDomain(getUrl()))
                            || link.getHref().startsWith("/")
                            || link.getHref().startsWith("")
                            || link.getHref().startsWith("#")
                        ? "internal" : "external";
            link.setType(type);
            links.add(link);
        });
        return links;
    }

    private String getProtocol(String url) {
        return url.startsWith(HTTPS) ? PROTOCOL_HTTPS : PROTOCOL_HTTP;
    }

    private String getDomain(String url) {

        //replace https or http if there
        url = url.replaceFirst(PROTOCOL_HTTPS,"").replaceFirst(PROTOCOL_HTTP,"");
        //remove all the path if any And remove the query string if path is not there
        url = url.contains("/") ? url.substring(0,url.indexOf("/")) : url.contains("?") ? url.substring(0,url.indexOf("?")) : url;
        return url;
    }

    public boolean detectLoginPage(Document htmlDocument) {
        List<Element> forms = htmlDocument.getElementsByTag("form");

        Element isLogin = forms.parallelStream().filter(form -> form.attr("method").equalsIgnoreCase("post"))
                              .filter(form -> isLoginPage(form)).findAny().orElse(null);

        return null != isLogin;
    }

    private boolean isLoginPage(Element form) {
        List<Element> inputField = form.getElementsByTag("input");
        List<Element> passwordField = inputField.parallelStream().filter(element -> element.attr("type").equalsIgnoreCase("password")).collect(Collectors.toList());
        return passwordField.size() == 1 ;
    }

    public Map<String,String> varifyLinks(List<Link> links) {
        Map<String,String> linksMap = new HashMap<>();
        links.parallelStream().forEach(link -> {
            try {
                if(link.getType().equalsIgnoreCase("internal")
                        && (link.getHref().startsWith("/") || link.getHref().startsWith("")|| link.getHref().startsWith("#"))) {
                    link.setHref(getDomain(getUrl())+link.getHref());
                }
                getHtmlDocument(link.getHref());
                linksMap.put(link.getHref(),"accessable");
            } catch (IOException e) {
                linksMap.put(link.getHref(),"non-accessable. "+e);
            }
        });
        return linksMap;
    }
}
