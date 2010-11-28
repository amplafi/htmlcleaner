package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.File;

/**
 * Testing XPath expressions against TagNodes results from cleaning process.
 */
public class XPatherTest extends TestCase {

    private TagNode rootNode;

    protected void setUp() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        rootNode = cleaner.clean( new File("src/test/resources/test5.html") );
    }

    public void testPathExpression() throws XPatherException {
        assertTrue( rootNode.evaluateXPath( "//div//a" ).length == 160 );
        assertStringArray(
            rootNode.evaluateXPath("//div//a[@id][@class]"),
            new Object[] { "Ocean", "More Yahoo! Services" }
        );
        assertStringArray(
            rootNode.evaluateXPath("/body/*[1]/@type"),
            new Object[] { "text/javascript" }
        );
        assertStringArray(
            rootNode.evaluateXPath("//div[3]//a[@id]"),
            new Object[] { "In the News", "World", "Local", "Finance" }
        );
        assertStringArray( rootNode.evaluateXPath("//div[3]//a[@id][@href='r/n4']"), new Object[] { "Local" } );
        assertStringArray(
            rootNode.evaluateXPath("//div[3]//a['video'=@class]"),
            new Object[] {
                    "An on-court proposal",
                    "See a one-armed basketball champ ",
                    "Israeli police raid the home of gunman behind school shooting",
                    "Clinton continues to question Obama's experience",
                    "Zero emission sports car unveiled at Switzerland auto show",
            }
        );
        assertStringArray(
            rootNode.evaluateXPath("//div[3]//a[@style]/..//li[a]"),
            new Object[] { "News", "Popular", "Election '08" }
        );
        assertStringArray( rootNode.evaluateXPath("(//body//div[3][@class]/span)[4]/@id"), new Object[] { "featured4ct" } );
        assertStringArray(
                rootNode.evaluateXPath("//body//div[3][@class]//span[2]/@id"), 
                new Object[] { "featured2ct", "worldnewsct" }
        );
        assertStringArray(
                rootNode.evaluateXPath("(//div[last() >= 4]//./div[position() = last()])[position() > 22]//li[2]//a"),
                new Object[] { "Awesome Chicken Noodle...", "Celebrity Rehab", "24" }
        );
        assertEquals( rootNode.evaluateXPath("//*[@class][@id]//*[@style]").length, 23 );
        assertEquals( rootNode.evaluateXPath("//div/@class").length, 43 );
        assertEquals( rootNode.evaluateXPath("//div//@class").length, 130 );
        assertStringArray(
                rootNode.evaluateXPath("(//div[@id]//@class)[position() < 5]"),
                new Object[] { "eyebrowborder", "mastheadbd", "iemw", "ac_container" }
        );
        assertEquals( rootNode.evaluateXPath("//div[2]/@*").length, 33 );
        assertStringArray(
                rootNode.evaluateXPath("//div[2]/@*[2]"),
                new Object[] { "ad", "bd", "bd", "papreviewdiv", "ad", "bd", "bd" }
        );
        assertStringArray(
                rootNode.evaluateXPath("//div[2]//a[. = \"Images\"]/@href"),
                new Object[] { "r/00/*-http://images.search.yahoo.com/search/images" }
        );
    }

    public void testFunctions() throws XPatherException {
        assertNumber( rootNode.evaluateXPath("count(//div//img)"), 26 );
        assertStringArray(
            rootNode.evaluateXPath("data(//div//a[@id][@class])"),
            new Object[] { "Ocean", "More Yahoo! Services" }
        );
        assertStringArray( rootNode.evaluateXPath("count(//a)"), new Object[] { "160" } );
        assertStringArray(
            rootNode.evaluateXPath("//p/last()"),
            new Object[] { "2", "2" }
        );
        assertStringArray(
            rootNode.evaluateXPath("//style/position()"),
            new Object[] { "1", "2", "3", "4", "5", "6", "7" }
        );
        assertStringArray(
            rootNode.evaluateXPath("//body//div[3][@class]//span[last()<=4]/@id"),
            new Object[] { "inthenews2ct", "worldnewsct", "localnewsct", "finsnewsct" }
        );
        assertStringArray(
            rootNode.evaluateXPath("//body//div[3][@class]//span[12.2<position()]/@id"),
            new Object[] { "money1ct", "money2ct", "money3ct", "money4ct" }
        );
        assertStringArray(
            rootNode.evaluateXPath("//div//../span[position() = 2]/@id"),
            new Object[] { "featured2ct", "footer2", "worldnewsct" }
        );
        assertStringArray(
            rootNode.evaluateXPath("data(//div[last() >= 4][position() <= 2]//li[4]//a)"),
            new Object[] { "Video", "7 top cities for a great weekend trip", "Chrysler", "Jeep", "Saturn", "Insurance" }
        );
        assertStringArray(
            rootNode.evaluateXPath("//a['v' < @id]/@id"),
            new Object[] { "vsearchmore", "worldnews" }
        );
        assertStringArray(
            rootNode.evaluateXPath("data(//a['v' < @id])"),
            new Object[] { "More", "World" }
        );
    }

    private void assertNumber(Object array[], double number) {
        assertTrue(array != null);
        assertTrue(array.length == 1);
        assertTrue(array[0] instanceof Number);
        assertTrue(array[0] instanceof Number);
        assertTrue(((Number)array[0]).doubleValue() == number);
    }

    private void assertStringArray(Object array1[], Object array2[]) {
        assertNotNull( array1 );
        assertNotNull( array2 );
        assertEquals( array1.length, array2.length );
        for (int i = 0; i < array1.length; i++) {
            assertNotNull( array1[i] );
            assertNotNull( array2[i] );
            String s1 = array1[i] instanceof TagNode ? ((TagNode)array1[i]).getText().toString() : array1[i].toString();
            String s2 = array2[i] instanceof TagNode ? ((TagNode)array2[i]).getText().toString() : array2[i].toString();
            assertEquals(s1, s2);
        }
    }

}
