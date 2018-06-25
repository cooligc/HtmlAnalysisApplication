package com.skc.scout24;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController {


    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    @Autowired
    HtmlParsing htmlParsing;

    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST})
    public String landPage(){
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("Landing on Home Page");
        }
        return "ops/land";
    }

    @GetMapping("/data")
    public String htmlAnalysis(@RequestParam(value = "url",required = true)  String url, Model model) throws IOException{
        htmlParsing.setUrl(url);
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Entered URL by user --> "+ url);
        }

        Document document = htmlParsing.getHtmlDocument();
        List<Heading> headingList = htmlParsing.getHeadings(document);
        List<Link> allLinks = htmlParsing.getAllLinks(document);

        HtmlAnalysisModel responseModel = HtmlParsingResponseBuilder.init(new HtmlAnalysisModel())
                .withHtmlVersion(htmlParsing.getHtmlVersion(document))
                .withPageTitle(htmlParsing.getPageTitle(document))
                .withHeadingDetails(headingList,htmlParsing.getHeadingCount(headingList))
                .withLinksDetails(allLinks)
                .isLoginPage(htmlParsing.detectLoginPage(document))
                .build();
        model.addAttribute("response",responseModel);
        model.addAttribute("url",url);
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Analysis is done .");
        }

            return "ops/stats";
    }

}
