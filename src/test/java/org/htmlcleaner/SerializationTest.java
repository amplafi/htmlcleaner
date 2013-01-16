package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Testing node manipulation after cleaning.
 */
public class SerializationTest extends TestCase {

    private HtmlCleaner cleaner;
    private CleanerProperties properties;

    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
        properties = cleaner.getProperties();
    }

    public void testSerialization() throws XPatherException, IOException, ParserConfigurationException {
        TagNode node = cleaner.clean( new File("src/test/resources/test6.html"), "UTF-8" );
        final Document dom1 = new DomSerializer(properties, true).createDOM(node);
        final Document dom2 = new DomSerializer(properties, false).createDOM(node);
        final org.jdom.Document jdom1 = new JDomSerializer(properties, true).createJDom(node);
        final org.jdom.Document jdom2 = new JDomSerializer(properties, false).createJDom(node);

        String xml1 = new PrettyXmlSerializer(properties, "----").getAsString(node);
        assertTrue(xml1.indexOf("--------<mama:div xmlns:mama=\"http://www.helloworld.com\">") > 0);
        assertTrue(xml1.indexOf("----------------<sub>a</sub>") > 0);
        assertTrue(xml1.indexOf("--------<!-- ZANZIBAR '\"&<>' -->") > 0);
        assertTrue(xml1.indexOf("------------<x:button onclick=\"micko()\" xmlns:x=\"x\">PRITISNI</x:button>") > 0);

        String xml2 = new PrettyXmlSerializer(properties, "").getAsString(node);
        assertTrue(xml2.indexOf("\n<mama:div xmlns:mama=\"http://www.helloworld.com\">\n") > 0);

        node = cleaner.clean( new File("src/test/resources/test9.html") );
        String xml3 = new CompactXmlSerializer(properties).getAsString(node);
        assertTrue(xml3.indexOf("Moja mala nema mane...") >= 0);

        node = cleaner.clean( new File("src/test/resources/test6.html") );
        properties.setTransSpecialEntitiesToNCR(true);
        CompactXmlSerializer compactXmlSerializer = new CompactXmlSerializer(properties);
        assertTrue(compactXmlSerializer.getAsString(node).indexOf("<div>[&#945;][&#233;][&#8254;]</div>") >= 0);
        properties.setTransSpecialEntitiesToNCR(false);
        assertTrue(compactXmlSerializer.getAsString(node).indexOf("<div>[&#945;][&#233;][&#8254;]</div>") < 0);
    }
    
    public void testHexEntity() throws IOException  {
        final CleanerProperties props = new CleanerProperties();
        props.setRecognizeUnicodeChars(false);
        props.setOmitXmlDeclaration(true);
        
        final TagNode tagNode = new HtmlCleaner(props).clean("<html><body>&#x27;&#xa1;</body></html>");
        assertEquals("<html><head /><body>&#x27;&#xa1;</body></html>",
            new SimpleXmlSerializer(props).getAsString(tagNode, "utf-8"));
    }
}