package com.skc.scout24;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.google.common.collect.Lists;

/***
 * This is a kind of utility class for HTML where we will calculate the required
 * statistics
 * 
 * @author sitakanta
 *
 */
public class HtmlParsing {

	private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

	public static final String HTTPS = "https";
	public static final String PROTOCOL_HTTPS = "https://";
	public static final String PROTOCOL_HTTP = "http://";
	private static final List<String> STATIC_URLS = Arrays.asList("javascript", "mailto");

	/***
	 * This method is having only capability to hit to the web page
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Document getHtmlDocument(String url) throws IOException {

		url = validateURL(url, url);

		return Jsoup.connect(url).get();
	}

	/**
	 * This method will validate the URL and do the needful. e.g if url starts with
	 * // , then it will add http:// at front and wipe out //
	 * 
	 * @param url
	 *            This might be relative url
	 * @param requestURL
	 *            is the real request url
	 * @return
	 */
	public static String validateURL(String url, String requestURL) {
		// Case //www.youtube.com
		if (url.startsWith("//")) {
			url = url.replaceFirst("//", PROTOCOL_HTTP);
		}

		// Case index.html
		if (!url.trim().contains("/")) {
			url = "/" + url;
		}

		// Case /about
		if (url.trim().startsWith("/")) {
			url = getProtocol(requestURL) + getDomain(requestURL) + "/" + url;
		}
		return url;
	}

	/**
	 * This method will retrieve the HTML version from the {@link Document}
	 * 
	 * @param document
	 * @return
	 * @throws IOException
	 */
	public static String getHtmlVersion(Document document) throws IOException {
		assert document != null;
		List<Node> nodes = document.childNodes();
		return getHtmlVersion(nodes);
	}

	/***
	 * 
	 * @param nodes
	 * @return
	 */
	private static String getHtmlVersion(List<Node> nodes) {
		Optional<Node> _node = nodes.parallelStream().filter(node -> node instanceof DocumentType).findFirst();
		DocumentType documentType = (DocumentType) _node.get();
		// Version docs retrieve from https://www.w3schools.com/tags/tag_doctype.asp
		final Pattern pattern = Pattern.compile("([0-9]*[.])+");
		final Matcher matcher = pattern.matcher(documentType.outerHtml());
		if (!matcher.find()) {
			return "5";
		}
		return matcher.group(1).substring(0, 1);
	}

	/**
	 * This method is having responsibility to retrieve HTML title from
	 * {@link Document}
	 * 
	 * @param document
	 * @return
	 * @throws IOException
	 */
	public static String getPageTitle(Document document) throws IOException {
		assert document != null;
		return document.title();
	}

	/**
	 * This method will get all the heading tags and will scan it from
	 * {@link Document} and retrieve the {@link List} of {@link Heading}
	 * 
	 * @param document
	 * @return
	 * @throws IOException
	 */
	public static List<Heading> getHeadings(final Document document) throws IOException {
		assert document != null;
		List<Heading> headingList = new ArrayList<>();
		Arrays.asList("h1", "h2", "h3", "h4", "h5").parallelStream().forEach(tag -> {
			List<Element> elements = document.getElementsByTag(tag);
			// TODO Need to optimize
			elements.parallelStream().forEach(element -> {
				Heading heading = new Heading();
				heading.setType(tag);
				heading.setValue(element.ownText());
				headingList.add(heading);
			});
		});
		return headingList;
	}

	/**
	 * This method is responsible to get the HTML heading count and return into the
	 * map
	 * 
	 * @param headings
	 * @return
	 */
	public static Map<String, Long> getHeadingCount(List<Heading> headings) {
		Map<String, Long> headingCount = new HashMap<>();
		Arrays.asList("h1", "h2", "h3", "h4", "h5").parallelStream().forEach(tag -> {
			long count = headings.parallelStream().filter(heading -> heading.getType().equalsIgnoreCase(tag)).count();
			headingCount.put(tag, count);
		});
		return headingCount;
	}

	/**
	 * This method will retrieve all the links on a {@link Document}.
	 * 
	 * @param htmlDocument
	 * @return
	 */
	public static List<Link> getAllLinks(Document htmlDocument, String url) {
		List<Link> links = new ArrayList<>();
		List<Element> elements = htmlDocument.getElementsByTag("a");
		elements.parallelStream().forEach(element -> {
			Link link = new Link();
			link.setHref(element.attr("href"));

			String type = link.getHref().contains(getDomain(url)) || link.getHref().startsWith("/")
					|| link.getHref().equalsIgnoreCase("") || link.getHref().startsWith("#") ? "internal" : "external";
			link.setType(type);
			link.setAbsoluteURL(getModifiedURL(link, url));
			links.add(link);
		});
		varifyLinks(links, url);
		return links;
	}

	/**
	 * 
	 * @param link
	 * @param url
	 * @return
	 */
	// TODO Need to be optimized
	private static String getModifiedURL(Link link, String url) {
		String modifiedURL = link.getHref();
		if (checkStaticURL(modifiedURL)) {
			link.setIsAccessable(Boolean.FALSE);
			link.setErrorString("Inline static (e.g javascript: , mailto: etc) URL cannot be accessable");
			link.setStatusCode(-1);
			return modifiedURL;
		}

		// Case - //www.youtube.com
		if (modifiedURL.trim().startsWith("//")) {
			return modifiedURL;
		}

		// For Home page. just appaending #
		if (modifiedURL.length() == 0) {
			link.setHref("/");
		}

		if (link.getType().equalsIgnoreCase("internal") && !link.getHref().startsWith("http") &&
		// Few URL starts with //www.somedomain.com . This condition will remove such.
		// This is for / relative URL
				(link.getHref().startsWith("/") || link.getHref().startsWith("#"))) {
			modifiedURL = getProtocol(url) + getDomain(url) + link.getHref();
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Modified URL --> " + modifiedURL + " \t Before URL --->" + link.getHref() + "\t "
					+ link.getType());
		}
		return modifiedURL;
	}

	/**
	 * 
	 * @param modifiedURL
	 * @return
	 */
	private static boolean checkStaticURL(String modifiedURL) {
		// TODO Need to revisit
		for (String shtURL : STATIC_URLS) {
			if (modifiedURL.contains(shtURL)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	private static String getProtocol(String url) {
		return url.startsWith(HTTPS) ? PROTOCOL_HTTPS : PROTOCOL_HTTP;
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	private static String getDomain(String url) {

		// replace https or http if there
		url = url.replaceFirst(PROTOCOL_HTTPS, "").replaceFirst(PROTOCOL_HTTP, "");
		// remove all the path if any And remove the query string if path is not there
		url = url.contains("/") ? url.substring(0, url.indexOf("/"))
				: url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
		return url;
	}

	/**
	 * This method responsibility to detect Login form on {@link Document}
	 * 
	 * @param htmlDocument
	 * @return
	 */
	public static boolean detectLoginPage(Document htmlDocument) {
		List<Element> forms = htmlDocument.getElementsByTag("form");

		Element isLogin = forms.parallelStream().filter(form -> form.attr("method").equalsIgnoreCase("post"))
				.filter(form -> isLoginPage(form)).findAny().orElse(null);

		return null != isLogin;
	}

	/**
	 * 
	 * @param form
	 * @return
	 */
	private static boolean isLoginPage(Element form) {
		List<Element> inputField = form.getElementsByTag("input");
		List<Element> passwordField = inputField.parallelStream()
				.filter(element -> element.attr("type").equalsIgnoreCase("password")).collect(Collectors.toList());
		return passwordField.size() == 1;
	}

	/**
	 * This method will validate wheather the links are reachable or not
	 * 
	 * @param links
	 */
	public static void varifyLinks(List<Link> links, String url) {

		Map<String, Map<String, Object>> resourceValidationMap = new HashMap<>();

		// Consider performance here, I use ExecutorService as multi-thread pool to
		// check all the resources
		final int numOfThread = 9; // thread pool size set to (No. CPU + 1) is optimal on average.
		ExecutorService threadPool = Executors.newFixedThreadPool(numOfThread);

		// Create a list to hold the Future object associated with Callable
		List<FutureTask<Map<String, Map<String, Object>>>> futureTaskList = new ArrayList<>();

		// Use Guava Lists.partition to partition resource list into subsets. Amazing!
		List<List<Link>> dividedList = Lists.partition(links, numOfThread);

		for (List<Link> subList : dividedList) {
			URLValidation urlValidationTask = new URLValidation(subList, getDomain(url));
			FutureTask<Map<String, Map<String, Object>>> futureTask = new FutureTask<>(urlValidationTask);
			threadPool.submit(futureTask);
			futureTaskList.add(futureTask);
		}

		for (FutureTask<Map<String, Map<String, Object>>> completeFutureTask : futureTaskList) {
			try {
				resourceValidationMap.putAll(completeFutureTask.get());
			} catch (InterruptedException e) {
				LOGGER.error(e);
			} catch (ExecutionException e) {
				LOGGER.error(e);
			}
		}

		threadPool.shutdown();

		links.parallelStream().forEach(single -> {
			if (resourceValidationMap.get(single.getHref()) != null) {
				Map<String, Object> responseMap = resourceValidationMap.get(single.getHref());
				single.setStatusCode((Integer) responseMap.get("statusCode"));
				single.setIsAccessable((Boolean) responseMap.get("isAccessable"));
				String message = !single.getIsAccessable() ? (String) responseMap.get("message") : null;
				single.setErrorString(message);
			}
		});
	}

}
