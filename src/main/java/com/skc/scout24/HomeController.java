package com.skc.scout24;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/***
 * This is the Main Controller which will handle all business operations
 * 
 * @author sitakanta
 *
 */
@Controller
@RequestMapping("/home")
public class HomeController {


    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    
    /***
     * This will land on the Landing Page of application
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST})
    public String landPage(){
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("Landing on Home Page");
        }
        return "ops/land";
    }

    /***
     * This method will get the URL as a part of {@link RequestParam} and will process it further to get HTML specific statistics
     * 
     * @param url
     * @param model
     * @return
     * @throws IOException
     */
    @GetMapping("/data")
    public String htmlAnalysis(@RequestParam(value = "url",required = true)  String url, Model model) throws IOException{
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Entered URL by user --> "+ url);
        }

        Document document = HtmlParsing.getHtmlDocument(url);
        List<Heading> headingList = HtmlParsing.getHeadings(document);
        List<Link> allLinks = HtmlParsing.getAllLinks(document,url);

        HtmlAnalysisModel responseModel = HtmlParsingResponseBuilder.init(new HtmlAnalysisModel())
                .withHtmlVersion(HtmlParsing.getHtmlVersion(document))
                .withPageTitle(HtmlParsing.getPageTitle(document))
                .withHeadingDetails(headingList,HtmlParsing.getHeadingCount(headingList))
                .withLinksDetails(allLinks)
                .isLoginPage(HtmlParsing.detectLoginPage(document))
                .build();
        model.addAttribute("response",responseModel);
        model.addAttribute("url",url);
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Analysis is done .");
        }

        return "ops/stats";
    }

}
