package com.skc.scout24;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HtmlParsingTestCase {

    @Autowired
    HtmlParsing htmlParsing;

    @Before
    public void setUp(){
        htmlParsing.setUrl("https://github.com");
    }

    @Test
    public void testHtml5Version(){
        try {
            String htmlVersion = htmlParsing.getHtmlVersion(null);
            Assert.assertEquals("5",htmlVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHtmlVersion(){
        htmlParsing.setUrl("https://www.spiegel.de/meinspiegel/login.html");
        try {
            String version = htmlParsing.getHtmlVersion(null);
            Assert.assertEquals("4",version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPageTitle(){
        try {
            Assert.assertEquals("The world’s leading software development platform · GitHub",htmlParsing.getPageTitle(null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHeadings(){
        try {
            List<Heading> headingList = htmlParsing.getHeadings(htmlParsing.getHtmlDocument());
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
            List<Heading> headingList = htmlParsing.getHeadings(htmlParsing.getHtmlDocument());
            Map<String,Long> headingCount = htmlParsing.getHeadingCount(headingList);
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
            List<Link> links = htmlParsing.getAllLinks(htmlParsing.getHtmlDocument());
            Assert.assertTrue(links.size() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginPage(){
        try {
            boolean isLoginPage = htmlParsing.detectLoginPage(htmlParsing.getHtmlDocument());
            Assert.assertTrue(isLoginPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNotLoginPage(){
        htmlParsing.setUrl("https://www.youtube.com");
        try {
            boolean isLogin = htmlParsing.detectLoginPage(htmlParsing.getHtmlDocument());
            Assert.assertTrue(!isLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginPageSpec(){
        htmlParsing.setUrl("https://www.spiegel.de/meinspiegel/login.html");
        try {
            boolean isLogin = htmlParsing.detectLoginPage(htmlParsing.getHtmlDocument());
            Assert.assertTrue(isLogin);

            htmlParsing.setUrl("https://github.com/login");
            isLogin = htmlParsing.detectLoginPage(htmlParsing.getHtmlDocument());
            Assert.assertTrue(isLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVarifyLinks(){
        List<Link> links = new ArrayList<>();
        links.add(new Link("https://github.com","internal","https://github.com"));
        links.add(new Link("https://testareddcsj.com","external","https://testareddcsj.com"));
        htmlParsing.varifyLinks(links);
        links.parallelStream().forEach(link -> {
        	if(link.getType().equalsIgnoreCase("external")) {
        		Assert.assertEquals(link.getHref(), "https://testareddcsj.com");
        		Assert.assertTrue(!link.getIsAccessable());
        	}else {
        		Assert.assertEquals(link.getHref(), "https://github.com");
        		Assert.assertTrue(link.getIsAccessable());
        	}
        });
    }

    @Test
    public void testReacableURL(){

        try {
            Method method = htmlParsing.getClass().getDeclaredMethod("getModifiedURL", Link.class,String.class);

            Link link = new Link();
            link.setHref("/open-source/stories/freakboy3742");
            link.setType("internal");
            link.setAbsoluteURL("/about");
            method.setAccessible(true);
            String url = (String)method.invoke(htmlParsing, link,"https://github.com");
            Assert.assertEquals("https://github.com/open-source/stories/freakboy3742",url);

            link = new Link();
            link.setHref("#abc");
            link.setType("internal");
            link.setAbsoluteURL("#abc");
            method.setAccessible(true);
            url = (String)method.invoke(htmlParsing, link,"https://github.com");
            Assert.assertEquals("https://github.com#abc",url);

            Link link2 = new Link();
            link.setHref("//www.youtube.com");
            link.setType("external");
            method.setAccessible(true);
            url = (String)method.invoke(htmlParsing, link,"https://github.com");
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
