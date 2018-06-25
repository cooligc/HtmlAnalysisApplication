package com.skc.scout24;

import java.util.List;
import java.util.Map;

public class HtmlParsingResponseBuilder {
    private static HtmlAnalysisModel htmlAnalysisModel;

    private HtmlParsingResponseBuilder(HtmlAnalysisModel model){
        if(null == model){
            HtmlParsingResponseBuilder.htmlAnalysisModel = new HtmlAnalysisModel();
        }
        HtmlParsingResponseBuilder.htmlAnalysisModel = model;
    }

    public static HtmlParsingResponseBuilder init(HtmlAnalysisModel model){
        return new HtmlParsingResponseBuilder(model);
    }

    public HtmlParsingResponseBuilder withPageTitle(String pageTitle){
        HtmlParsingResponseBuilder.htmlAnalysisModel.setPageTitle(pageTitle);
        return this;
    }

    public HtmlParsingResponseBuilder withHtmlVersion(String version){
        HtmlParsingResponseBuilder.htmlAnalysisModel.setHtmlVersion(version);
        return this;
    }

    public HtmlParsingResponseBuilder withHeadingDetails(List<Heading> headingList, Map<String,Long> headingCountMap){
        HtmlParsingResponseBuilder.htmlAnalysisModel.setAllHeadings(headingList);
        HtmlParsingResponseBuilder.htmlAnalysisModel.setHeadingCountMap(headingCountMap);
        return this;
    }

    public HtmlParsingResponseBuilder withLinksDetails(List<Link>  allLinks){
        HtmlParsingResponseBuilder.htmlAnalysisModel.setAllLinks(allLinks);
        return this;
    }

    public HtmlParsingResponseBuilder isLoginPage(Boolean isLoginPage){
        HtmlParsingResponseBuilder.htmlAnalysisModel.setLoginPage(isLoginPage);
        return this;
    }

    public HtmlAnalysisModel build(){
        assert HtmlParsingResponseBuilder.htmlAnalysisModel != null;
        return HtmlParsingResponseBuilder.htmlAnalysisModel;
    }

}
