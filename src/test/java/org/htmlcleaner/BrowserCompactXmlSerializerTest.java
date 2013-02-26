package org.htmlcleaner;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test cases for for {@link BrowserCompactXmlSerializer}
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public class BrowserCompactXmlSerializerTest extends TestCase {

    private BrowserCompactXmlSerializer compactXmlSerializer;
    private CleanerProperties properties;

    @Override
    protected void setUp() throws Exception {
        properties = new CleanerProperties();
        properties.setOmitHtmlEnvelope(true);
        properties.setOmitXmlDeclaration(true);
        compactXmlSerializer = new BrowserCompactXmlSerializer(properties);
    }
    
    /**
     * Tests that serializer removes white spaces properly.
     * @throws IOException 
     */
    public void testRemoveInsignificantWhitespaces() throws IOException{
        String cleaned = compactXmlSerializer.getXmlAsString( "        <u>text here, </u><b>some text</b>      ", "UTF-8");
        assertEquals("<u>text here, </u><b>some text</b>", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( "    <div class=\"foo\">2 roots < here >  </div>", "UTF-8");
        assertEquals("<div class=\"foo\">2 roots &lt; here &gt;</div>\n", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( "    <div class=\"foo\">2 roots \n    < here >  </div>", "UTF-8");
        assertEquals("<div class=\"foo\">2 roots &lt; here &gt;</div>\n", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( "    <div class=\"foo\">2 roots \n\n    < here >  </div>", "UTF-8");
        assertEquals("<div class=\"foo\">2 roots <br />&lt; here &gt;</div>\n", cleaned);
    }
    
    /**
     * Non-breakable spaces also must be removed from start and end.
     * @throws IOException 
     */
    public void testRemoveLeadingAndEndingNbsp() throws IOException {
        String cleaned = compactXmlSerializer.getXmlAsString( 
                "&nbsp;&nbsp;We have just released Jericho Road. Listen to Still Waters the lead-off track.", "UTF-8");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( 
                "&#160;We have just released Jericho Road. Listen to Still Waters the lead-off track.&#160;", "UTF-8");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( 
                "&#xA0;We have just released Jericho Road. Listen to Still Waters the lead-off track.&#xA0;", "UTF-8");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( SpecialEntities.NON_BREAKABLE_SPACE
                + "We have just released Jericho Road. Listen to Still Waters the lead-off track.&#xA0;"
                + SpecialEntities.NON_BREAKABLE_SPACE, "UTF-8");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
    }
    
    /**
     * Tests that contents of 'pre' tag are untouched.
     * @throws IOException 
     */
    public void testPreTagIsUntouched() throws IOException{
        String cleaned = compactXmlSerializer.getXmlAsString( "   <pre>some text</pre>", "UTF-8");
        assertEquals("<pre>some text</pre>\n", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( "<pre>     some text</pre>", "UTF-8");
        assertEquals("<pre>     some text</pre>\n", cleaned);
        cleaned = compactXmlSerializer.getXmlAsString( "<pre>some /n/n text</pre>", "UTF-8");
        assertEquals("<pre>some /n/n text</pre>\n", cleaned);
    }
}
