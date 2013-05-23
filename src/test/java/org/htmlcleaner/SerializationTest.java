package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;

import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Testing node manipulation after cleaning.
 */
public class SerializationTest extends TestCase {

    private HtmlCleaner cleaner;
    private CleanerProperties properties;

    @Override
    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
        properties = cleaner.getProperties();
    }
    
    private TagNode getTestTagNode() throws IOException {
        TagNode node = cleaner.clean(new File("src/test/resources/test6.html"), "UTF-8");

        return node;
    }

    /**
     * Test if DomSerializer creates documents with a doctype where supplied. See issue #27
     * 
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public void testDomSerializerXhtml() throws ParserConfigurationException, IOException, TransformerException {
    	
        TagNode node = cleaner.clean(new File("src/test/resources/test10.html"), "UTF-8");

        final Document dom = new DomSerializer(properties, true).createDOM(node);
        
        assertEquals("html", dom.getDoctype().getName());
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", dom.getDoctype().getPublicId());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", dom.getDoctype().getSystemId());
    }
    
    /**
     * Test if DomSerializer creates documents with a doctype where supplied. See issue #27
     * 
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public void testDomSerializerHtml4() throws ParserConfigurationException, IOException, TransformerException {
    	
        TagNode node = cleaner.clean(new File("src/test/resources/test4.html"), "UTF-8");

        final Document dom = new DomSerializer(properties, true).createDOM(node);
        
        assertEquals("HTML", dom.getDoctype().getName());
        assertEquals("-//W3C//DTD HTML 4.01 Transitional//EN", dom.getDoctype().getPublicId());
        assertEquals("", dom.getDoctype().getSystemId());
    }
    
    /**
     * Test if DomSerializer creates documents without a DocType where there is none specified in the input
     * 
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public void testDomSerializerNoDocType() throws ParserConfigurationException, IOException, TransformerException {
        TagNode node = cleaner.clean(new File("src/test/resources/test2.html"), "UTF-8");
        final Document dom = new DomSerializer(properties, true).createDOM(node);
        assertEquals(null, dom.getDoctype());
    }
    
    /**
     * Test if we handle xml:lang with DomSerializer 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testXmlNameSpace() throws IOException, ParserConfigurationException{
        TagNode node = cleaner.clean(new File("src/test/resources/test10.html"), "UTF-8");
        cleaner.getProperties().setNamespacesAware(true);
        Document dom = new DomSerializer(properties, true).createDOM(node);
        String simpleXml = new SimpleXmlSerializer(cleaner.getProperties()).getAsString(node);
        String prettyXml = new PrettyXmlSerializer(cleaner.getProperties()).getAsString(node);
        String compactXml = new CompactXmlSerializer(cleaner.getProperties()).getAsString(node);
        assertFalse(simpleXml.contains("xmlns:xml"));
        assertFalse(prettyXml.contains("xmlns:xml"));
        assertFalse(compactXml.contains("xmlns:xml"));

    }


    //TODO This should be properly tested, not only constructor
    public void testJDomSerializer() throws ParserConfigurationException, IOException {
        final org.jdom2.Document jdom1 = new JDomSerializer(properties, true).createJDom(getTestTagNode());
        final org.jdom2.Document jdom2 = new JDomSerializer(properties, false).createJDom(getTestTagNode());
    }

    public void testPrettyXmlSerializer() throws IOException {
        TagNode node = getTestTagNode();
        String xml1 = new PrettyXmlSerializer(properties, "----").getAsString(node);
        assertTrue(xml1.indexOf("--------<mama:div xmlns:mama=\"http://www.helloworld.com\">") > 0);
        assertTrue(xml1.indexOf("----------------<sub>a</sub>") > 0);
        assertTrue(xml1.indexOf("--------<!-- ZANZIBAR '\"&<>' -->") > 0);
        assertTrue(xml1.indexOf("------------<x:button onclick=\"micko()\">PRITISNI</x:button>") > 0);

        String xml2 = new PrettyXmlSerializer(properties, "").getAsString(node);
        assertTrue(xml2.indexOf("\n<mama:div xmlns:mama=\"http://www.helloworld.com\">\n") > 0);

    }

    public void testCompactXmlSerializer() throws IOException {
        TagNode node = getTestTagNode();

        node = cleaner.clean(new File("src/test/resources/test9.html"));
        String xml3 = new CompactXmlSerializer(properties).getAsString(node);
        assertTrue(xml3.indexOf("Moja mala nema mane...") >= 0);

    }

}