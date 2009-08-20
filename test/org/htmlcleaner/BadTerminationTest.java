package org.htmlcleaner;

import junit.framework.TestCase;

/**
 * @author patmoore
 *
 */
public class BadTerminationTest extends TestCase{

    public void testHandleGarbageInTag() throws Exception {
        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setOmitHtmlEnvelope(true);
        cleanerProperties.setOmitXmlDeclaration(true);
        cleanerProperties.setUseEmptyElementTags(false);
        String output = new SimpleXmlSerializer().getXmlAsString(cleanerProperties, "<div></div id=\"foo\">", "UTF-8");
        assertEquals("<div></div>", output);
        output = new SimpleXmlSerializer().getXmlAsString(cleanerProperties, "<h3><u><strong>President’s Message</strong></u><div> </h3>", "UTF-8");
        assertEquals("<h3><u><strong>President’s Message</strong></u><div> </div></h3>", output);

        // </{whitespace} is treated like a comment by FF
        // Maybe remove the whitespace?
//        output = new SimpleXmlSerializer().getXmlAsString(cleanerProperties, "<div></ div id=\"foo\">", "UTF-8");
//        assertEquals("<div></div>", output);
    }
}
