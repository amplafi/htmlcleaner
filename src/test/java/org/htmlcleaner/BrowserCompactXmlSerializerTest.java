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
        String cleaned = compactXmlSerializer.getAsString( "        <u>text here, </u><b>some text</b>      ");
        assertEquals("<u>text here, </u><b>some text</b>", cleaned);
        cleaned = compactXmlSerializer.getAsString( "    <div class=\"foo\">2 roots < here >  </div>");
        assertEquals("<div class=\"foo\">2 roots &lt; here &gt;</div>\n", cleaned);
        cleaned = compactXmlSerializer.getAsString( "    <div class=\"foo\">2 roots \n    < here >  </div>");
        assertEquals("<div class=\"foo\">2 roots &lt; here &gt;</div>\n", cleaned);
        cleaned = compactXmlSerializer.getAsString( "    <div class=\"foo\">2 roots \n\n    < here >  </div>");
        assertEquals("<div class=\"foo\">2 roots <br />&lt; here &gt;</div>\n", cleaned);
    }
    
    /**
     * Non-breakable spaces also must be removed from start and end.
     * @throws IOException 
     */
    public void testRemoveLeadingAndEndingNbsp() throws IOException {
        String cleaned = compactXmlSerializer.getAsString( 
                "&nbsp;&nbsp;We have just released Jericho Road. Listen to Still Waters the lead-off track.");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
        cleaned = compactXmlSerializer.getAsString( 
                "&#160;We have just released Jericho Road. Listen to Still Waters the lead-off track.&#160;");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
        cleaned = compactXmlSerializer.getAsString( 
                "&#xA0;We have just released Jericho Road. Listen to Still Waters the lead-off track.&#xA0;");
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
        cleaned = compactXmlSerializer.getAsString( SpecialEntities.NON_BREAKABLE_SPACE
                + "We have just released Jericho Road. Listen to Still Waters the lead-off track.&#xA0;"
                + SpecialEntities.NON_BREAKABLE_SPACE);
        assertEquals("We have just released Jericho Road. Listen to Still Waters the lead-off track.", cleaned);
    }
    
    /**
     * Tests that contents of 'pre' tag are untouched.
     * @throws IOException 
     */
    public void testPreTagIsUntouched() throws IOException{
        String cleaned = compactXmlSerializer.getAsString( "   <pre>some text</pre>");
        assertEquals("<pre>some text</pre>\n", cleaned);
        cleaned = compactXmlSerializer.getAsString( "<pre>     some text</pre>");
        assertEquals("<pre>     some text</pre>\n", cleaned);
        cleaned = compactXmlSerializer.getAsString( "<pre>some /n/n text</pre>");
        assertEquals("<pre>some /n/n text</pre>\n", cleaned);
    }
}
