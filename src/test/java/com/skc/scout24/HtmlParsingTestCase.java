package com.skc.scout24;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HtmlParsingTestCase {

    private String url = null;

    @Before
    public void setUp(){
        url = "https://github.com";
    }

    @Test
    public void testHtml5Version(){
        try {
            String htmlVersion = HtmlParsing.getHtmlVersion(HtmlParsing.getHtmlDocument(url));
            Assert.assertEquals("5",htmlVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHtmlVersion(){
        try {
            String version = HtmlParsing.getHtmlVersion(HtmlParsing.getHtmlDocument("https://www.spiegel.de/meinspiegel/login.html"));
            Assert.assertEquals("4",version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPageTitle(){
        try {
            Assert.assertEquals("The world’s leading software development platform · GitHub",HtmlParsing.getPageTitle(HtmlParsing.getHtmlDocument(url)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHeadings(){
        try {
            List<Heading> headingList = HtmlParsing.getHeadings(HtmlParsing.getHtmlDocument(url));
            headingList.parallelStream().forEach(heading -> {
                Assert.assertTrue(heading.getType().startsWith("h"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHeadingCount(){
        try {
            List<Heading> headingList = HtmlParsing.getHeadings(HtmlParsing.getHtmlDocument(url));
            Map<String,Long> headingCount = HtmlParsing.getHeadingCount(headingList);
            headingCount.forEach((key,value) -> {
                Assert.assertTrue(key.contains("h"));
                Assert.assertTrue(value > 0 || value == 0);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLinks() {
        try {
            List<Link> links = HtmlParsing.getAllLinks(HtmlParsing.getHtmlDocument(url),url);
            Assert.assertTrue(links.size() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginPage(){
        try {
            boolean isLoginPage = HtmlParsing.detectLoginPage(HtmlParsing.getHtmlDocument(url));
            Assert.assertTrue(isLoginPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNotLoginPage(){
        try {
            boolean isLogin = HtmlParsing.detectLoginPage(HtmlParsing.getHtmlDocument("http://www.sitakant.info"));
            Assert.assertTrue(!isLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginPageSpec(){
        try {
            boolean isLogin = HtmlParsing.detectLoginPage(HtmlParsing.getHtmlDocument("https://www.spiegel.de/meinspiegel/login.html"));
            Assert.assertTrue(isLogin);

            isLogin = HtmlParsing.detectLoginPage(HtmlParsing.getHtmlDocument("https://github.com/login"));
            Assert.assertTrue(isLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVarifyLinks(){
        List<Link> links = new ArrayList<>();
        links.add(new Link(url,"internal",url));
        //Expect 401 which is unaccessable
        links.add(new Link("https://books.google.co.in/bkshp?hl=en&tab=wp","external","https://books.google.co.in/bkshp?hl=en&tab=wp"));
        HtmlParsing.varifyLinks(links,url);
        links.parallelStream().forEach(link -> {
        	if(link.getType().equalsIgnoreCase("external")) {
        		Assert.assertEquals(link.getHref(), "https://books.google.co.in/bkshp?hl=en&tab=wp");
        		Assert.assertTrue(!link.getIsAccessable());
        	}else {
        		Assert.assertEquals(link.getHref(), url);
        		Assert.assertTrue(link.getIsAccessable());
        	}
        });
    }

    @Test
    public void testReacableURL(){

        try {
            Method method = HtmlParsing.class.getDeclaredMethod("getModifiedURL", Link.class,String.class);

            Link link = new Link();
            link.setHref("/open-source/stories/freakboy3742");
            link.setType("internal");
            link.setAbsoluteURL("/about");
            method.setAccessible(true);
            String url = (String)method.invoke(null, link,"https://github.com");
            Assert.assertEquals("https://github.com/open-source/stories/freakboy3742",url);

            link = new Link();
            link.setHref("#abc");
            link.setType("internal");
            link.setAbsoluteURL("#abc");
            method.setAccessible(true);
            url = (String)method.invoke(null, link,"https://github.com");
            Assert.assertEquals("https://github.com#abc",url);

            link = new Link();
            link.setHref("//www.youtube.com");
            link.setType("external");
            method.setAccessible(true);
            url = (String)method.invoke(null, link,"https://github.com");
            Assert.assertEquals("//www.youtube.com",url);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


}
