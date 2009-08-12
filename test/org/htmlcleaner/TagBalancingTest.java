package org.htmlcleaner;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;

/**
 * Tests parsing and tag balancing.  
 */
public class TagBalancingTest extends TestCase {

    protected void setUp() throws Exception {
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
        assertHtml(new File("test/org/htmlcleaner/files/test3.html"), "/head/noscript/meta/@http-equiv", "Refresh");
        assertHtml(new File("test/org/htmlcleaner/files/test3.html"), "count(/head/*)", "24");
        assertHtml(new File("test/org/htmlcleaner/files/test3.html"), "/head/meta[1]/@name", "verify-v1");
        assertHtml(new File("test/org/htmlcleaner/files/test3.html"), "/head/script[last()]/@language", "javascript1.1");
        assertHtml(new File("test/org/htmlcleaner/files/test7.html"), "/head/noscript/meta/@http-equiv", "refresh");
    }

    public void testTagProviders() throws XPatherException, IOException {
        HtmlCleaner cleaner1 = new HtmlCleaner();
        HtmlCleaner cleaner2 = new HtmlCleaner(new ConfigFileTagProvider(new File("default.xml")));
        SimpleXmlSerializer serializer = new SimpleXmlSerializer(cleaner1.getProperties());

        String s1 = serializer.getXmlAsString(cleaner1.clean(new File("test/org/htmlcleaner/files/test5.html")));
        String s2 = serializer.getXmlAsString(cleaner2.clean(new File("test/org/htmlcleaner/files/test5.html")));
        assertEquals(s1, s2);

        s1 = serializer.getXmlAsString(cleaner1.clean(new File("test/org/htmlcleaner/files/test1.html")));
        s2 = serializer.getXmlAsString(cleaner2.clean(new File("test/org/htmlcleaner/files/test1.html")));
        assertEquals(s1, s2);

        s1 = serializer.getXmlAsString(cleaner1.clean(new File("test/org/htmlcleaner/files/test2.html")));
        s2 = serializer.getXmlAsString(cleaner2.clean(new File("test/org/htmlcleaner/files/test2.html")));
        assertEquals(s1, s2);

        s1 = serializer.getXmlAsString(cleaner1.clean(new File("test/org/htmlcleaner/files/test3.html")));
        s2 = serializer.getXmlAsString(cleaner2.clean(new File("test/org/htmlcleaner/files/test3.html")));
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