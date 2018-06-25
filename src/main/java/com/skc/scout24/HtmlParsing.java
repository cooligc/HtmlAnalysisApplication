package com.skc.scout24;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.stereotype.Component;

@Component
public class HtmlParsing {

    public static final String HTTPS = "https";
    public static final String PROTOCOL_HTTPS = "https://";
    public static final String PROTOCOL_HTTP = "http://";
    private static final List<String> STATIC_URLS = Arrays.asList("javascript","mailto");
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
    	if(url.startsWith("//")) {
    		url = url.replaceFirst("//", PROTOCOL_HTTP);
    	}
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
                            || link.getHref().equalsIgnoreCase("") 
                            || link.getHref().startsWith("#")
                        ? "internal" : "external";
            link.setType(type);
            link.setAbsoluteURL(getModifiedURL(link,url));
            links.add(link);
        });
        varifyLinks(links);
        return links;
    }
    
    //TODO Need to be optimized
    private String getModifiedURL(Link link,String url) {
    	String modifiedURL=link.getHref();
    	String uuid = UUID.randomUUID().toString();
    	if(checkStaticURL(modifiedURL)) {
    		link.setIsAccessable(Boolean.FALSE);
    		link.setErrorString("Inline Java Script Code cannot be accessable");
    		return modifiedURL;
    	}
    	
    	//For Home page. just appaending # 
    	if(modifiedURL.length() == 0) {
    		link.setHref("#");
    	}
    	if(modifiedURL.equalsIgnoreCase("/advanced_search?hl=en-IN&fg=1"))
    	
    	System.out.println("1 - "+uuid+"\t"+modifiedURL);
    	if(link.getType().equalsIgnoreCase("internal") && !link.getHref().startsWith("http") && 
    			// Few URL starts with //www.somedomain.com . This condition will remove such. This is for / relative URL
    			((link.getHref().startsWith("/",1) && !link.getHref().startsWith("/",2))
                  || link.getHref().startsWith("#"))) {
    		modifiedURL = getProtocol(url)+getDomain(url)+link.getHref();
    	}
    	System.out.println("2 - "+uuid+"\t"+modifiedURL);
		return modifiedURL;
	}

	private boolean checkStaticURL(String modifiedURL) {
		//TODO Need to revisit
		for (String shtURL : STATIC_URLS) {
			if(modifiedURL.contains(shtURL)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
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

    public void varifyLinks(List<Link> links) {
        links.parallelStream().forEach(link -> {
            try {
            	System.out.println("links ---> "+link.getAbsoluteURL());
            	//If there is an inline javascript we should ignore to access it
            	if(link.getIsAccessable() == null ) {
                    getHtmlDocument(link.getAbsoluteURL());
                    link.setIsAccessable(Boolean.TRUE);
            	}
            } catch (IOException e) {
            	link.setIsAccessable(Boolean.FALSE);
            	link.setErrorString(e.toString());
            }
        });
    }
}
