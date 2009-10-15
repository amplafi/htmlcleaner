package org.htmlcleaner;

import junit.framework.TestCase;

/**
 * Test cases for for {@link BrowserCompactXmlSerializer}
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public class BrowserCompactXmlSerializerTest extends TestCase {

    /**
     * Tests that serializer removes white spaces properly.
     */
    public void testRemoveInsignificantWhitespaces(){
        CleanerProperties properties = new CleanerProperties();
        properties.setOmitHtmlEnvelope(true);
        properties.setOmitXmlDeclaration(true);
        BrowserCompactXmlSerializer compactXmlSerializer = new BrowserCompactXmlSerializer(properties);
        String cleaned = compactXmlSerializer.getXmlAsString(properties, "        <u>text here, </u><b>some text</b>      ", "UTF-8");
        assertEquals(" <u>text here, </u><b>some text</b> ", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString(properties, "    <div class=\"foo\">2 roots < here ></div>", "UTF-8");
        assertEquals(" <div class=\"foo\">2 roots &lt; here &gt;</div>\n", cleaned);
    }
}
