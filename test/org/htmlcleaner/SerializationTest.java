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
        TagNode node = cleaner.clean( new File("test/org/htmlcleaner/files/test6.html"), "UTF-8" );
        final Document dom1 = new DomSerializer(properties, true).createDOM(node);
        final Document dom2 = new DomSerializer(properties, false).createDOM(node);
        final org.jdom.Document jdom1 = new JDomSerializer(properties, true).createJDom(node);
        final org.jdom.Document jdom2 = new JDomSerializer(properties, false).createJDom(node);
    }

}