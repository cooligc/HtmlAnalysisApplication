package com.skc.scout24;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/***
 * This is the Model class which will take the HTML Statistics data to the UI Layer
 * @author sitakanta
 *
 */
public class HtmlAnalysisModel implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String htmlVersion;
    private String pageTitle;
    private List<Heading> allHeadings;
    private Map<String,Long> headingCountMap;
    private List<Link> allLinks;
    private Boolean isLoginPage;

    public List<Heading> getAllHeadings() {
        return allHeadings;
    }

    public void setAllHeadings(List<Heading> allHeadings) {
        this.allHeadings = allHeadings;
    }

    public String getHtmlVersion() {
        return htmlVersion;
    }

    public void setHtmlVersion(String htmlVersion) {
        this.htmlVersion = htmlVersion;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public Map<String, Long> getHeadingCountMap() {
        return headingCountMap;
    }

    public void setHeadingCountMap(Map<String, Long> headingCountMap) {
        this.headingCountMap = headingCountMap;
    }

    public List<Link> getAllLinks() {
        return allLinks;
    }

    public void setAllLinks(List<Link> allLinks) {
        this.allLinks = allLinks;
    }

    public Boolean getLoginPage() {
        return isLoginPage;
    }

    public void setLoginPage(Boolean loginPage) {
        isLoginPage = loginPage;
    }
}
