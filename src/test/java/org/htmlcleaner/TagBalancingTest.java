package org.htmlcleaner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.tools.ant.util.FileUtils;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Tests parsing and tag balancing.
 */
public class TagBalancingTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
    }
    
    public void testShouldReopenTagHavingItemsToMove() throws XPatherException, IOException  {
    	HtmlCleaner cleaner = new HtmlCleaner();
    	cleaner.getProperties().setOmitXmlDeclaration(true);
    	cleaner.getProperties().setOmitComments(true);
    	SimpleXmlSerializer serializer = new SimpleXmlSerializer(cleaner.getProperties());
    	
    	String expected = FileUtils.readFully(new FileReader((new File("src/test/resources/reopenTagHavingItemsToMove-cleaned.html"))));
    	String actual = serializer.getXmlAsString(cleaner.clean(new File("src/test/resources/reopenTagHavingItemsToMove.html")));
    	assertEquals(expected.trim(), actual.trim());
    }
    
    public void testShouldSupportBreakingSeveralOpenTags() throws XPatherException, IOException {
    	HtmlCleaner cleaner = new HtmlCleaner();
    	cleaner.getProperties().setOmitXmlDeclaration(true);
    	cleaner.getProperties().setOmitComments(true);
    	SimpleXmlSerializer serializer = new SimpleXmlSerializer(cleaner.getProperties());
    	
    	String expected = FileUtils.readFully(new FileReader((new File("src/test/resources/severalTagsClosedByChildBreak-cleaned.html"))));
    	String actual = serializer.getXmlAsString(cleaner.clean(new File("src/test/resources/severalTagsClosedByChildBreak.html")));
    	
    	assertEquals(expected.trim(), actual.trim());
    }
    
    public void testBalancing() throws XPatherException, IOException {
        assertHtml(
                "<u>aa<i>a<b>at</u> fi</i>rst</b> text",
                "<html><head /><body><u>aa<i>a<b>at</b></i></u><i><b>fi</b></i><b>rst</b>text</body></html>"
        );
        assertHtml(
                "<u>a<big>a<i>a<b>at<sup></u> fi</big>rst</b> text",
                "<html><head /><body><u>a<big>a<i>a<b>at<sup /></b></i></big></u><big><i><b><sup>fi</sup>" +
                        "</b></i></big><i><b><sup>rst</sup></b><sup>text</sup></i></body></html>"
        );
        assertHtml(
            "<u><big><i>a",
            "<html><head /><body><u><big><i>a</i></big></u></body></html>"
        );
        assertHtml(new File("src/test/resources/test3.html"), "/head/noscript/meta/@http-equiv", "Refresh");
        assertHtml(new File("src/test/resources/test3.html"), "count(/head/*)", "24");
        assertHtml(new File("src/test/resources/test3.html"), "/head/meta[1]/@name", "verify-v1");
        assertHtml(new File("src/test/resources/test3.html"), "/head/script[last()]/@language", "javascript1.1");
        assertHtml(new File("src/test/resources/test7.html"), "/head/noscript/meta/@http-equiv", "refresh");
    }

    public void testTagProviders() throws IOException {
        HtmlCleaner cleaner1 = new HtmlCleaner();
        HtmlCleaner cleaner2 = new HtmlCleaner(new ConfigFileTagProvider(new File("default.xml")));
        SimpleXmlSerializer serializer = new SimpleXmlSerializer(cleaner1.getProperties());

        String s1 = serializer.getXmlAsString(cleaner1.clean(new File("src/test/resources/test5.html")));
        String s2 = serializer.getXmlAsString(cleaner2.clean(new File("src/test/resources/test5.html")));
        assertEquals(s1, s2);

        s1 = serializer.getXmlAsString(cleaner1.clean(new File("src/test/resources/test1.html")));
        s2 = serializer.getXmlAsString(cleaner2.clean(new File("src/test/resources/test1.html")));
        assertEquals(s1, s2);

        s1 = serializer.getXmlAsString(cleaner1.clean(new File("src/test/resources/test2.html")));
        s2 = serializer.getXmlAsString(cleaner2.clean(new File("src/test/resources/test2.html")));
        assertEquals(s1, s2);

        s1 = serializer.getXmlAsString(cleaner1.clean(new File("src/test/resources/test3.html")));
        s2 = serializer.getXmlAsString(cleaner2.clean(new File("src/test/resources/test3.html")));
        assertEquals(s1, s2);
    }

    private String getJDomOutput(Reader reader) {
        SAXBuilder saxBuilder = new SAXBuilder();
        final Document document;
        try {
            document = saxBuilder.build(reader);
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getCompactFormat());
            return xmlOutputter.outputString(document);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getJDomOutput(String xml) {
        return getJDomOutput(new StringReader(xml));
    }

    private String getJDomOutput(File file) {
        try {
            return getJDomOutput(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void assertHtml(String html, String xml) throws IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setOmitXmlDeclaration(true);
        TagNode node = cleaner.clean(html);
        String result = new CompactXmlSerializer(cleaner.getProperties()).getXmlAsString(node);

        String s1 = getJDomOutput(result);
        String s2 = getJDomOutput(xml);

        assertEquals(s1, s2);
    }

    private void assertHtml(File html, File xml) throws IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setOmitXmlDeclaration(true);
        TagNode node = cleaner.clean(html);
        String result = new CompactXmlSerializer(cleaner.getProperties()).getXmlAsString(node);

        String s1 = getJDomOutput(result);
        String s2 = getJDomOutput(xml);

        assertEquals(s1, s2);
    }

    private void assertHtml(File html, String xpath, String value) throws IOException, XPatherException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setOmitXmlDeclaration(true);
        TagNode node = cleaner.clean(html);
        Object xPathResult[] = node.evaluateXPath(xpath);
        assertTrue(xPathResult.length >= 1);
        assertEquals(xPathResult[0].toString(), value);
    }

}