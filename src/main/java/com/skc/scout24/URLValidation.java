/**
 * 
 */
package com.skc.scout24;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;

/**
 * @author sitakanta
 *
 */
public class URLValidation implements Callable<Map<String, Map<String,Object>>> {
	
	private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

	private List<Link> links;
	private String url;

	public URLValidation(List<Link> links, String url) {
		this.links = links;
		this.url = url;
	}

	@Override
	public Map<String, Map<String,Object>> call() throws Exception {
		Map<String, Map<String,Object>> responseMap = new HashMap<>();
		for (Link link : links) {
			Map<String,Object> urlResponseMap = getResponseCode(link) ;
			int statusCode = (int) urlResponseMap.get("statusCode");
			Boolean isAccessable = (statusCode < 400 && statusCode > 0) ? Boolean.TRUE: Boolean.FALSE;
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("status code -- "+statusCode+" URL -->"+link.getHref() +" --> Accessable "+isAccessable);
			}
			
			link.setIsAccessable(isAccessable);
			link.setStatusCode(statusCode);
			Map<String,Object> modifiedMap = new HashMap<>();
			modifiedMap.put("statusCode", statusCode);
			modifiedMap.put("isAccessable", isAccessable);
			modifiedMap.put("message", urlResponseMap.get("message"));
			responseMap.put(link.getHref(), modifiedMap);
		}
		return responseMap;
	}

	public Map<String,Object> getResponseCode(Link link) throws IOException {
		Map<String,Object> urlResponseMap = new HashMap<>();
		String absoluteURL = HtmlParsing.validateURL(link.getAbsoluteURL(),url);
		URL url = new URL(absoluteURL);
		
		
		if ((url.openConnection() instanceof HttpURLConnection) && link.getIsAccessable()) {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			connection.setInstanceFollowRedirects(false);
			HttpURLConnection.setFollowRedirects(false);
			connection.connect();
			urlResponseMap.put("statusCode", connection.getResponseCode());
			urlResponseMap.put("message", connection.getResponseMessage());
			return urlResponseMap;
		}
		urlResponseMap.put("statusCode", -1);
		urlResponseMap.put("message", "Invalid request");
		return urlResponseMap;
	}
}
