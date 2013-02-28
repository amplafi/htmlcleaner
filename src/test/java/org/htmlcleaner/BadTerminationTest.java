package org.htmlcleaner;

import junit.framework.TestCase;

/**
 * @author patmoore
 * 
 */
public class BadTerminationTest extends TestCase {

    public void testHandleGarbageInEndTag() throws Exception {
        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setOmitHtmlEnvelope(true);
        cleanerProperties.setOmitXmlDeclaration(true);
        cleanerProperties.setUseEmptyElementTags(false);

        String output = new SimpleXmlSerializer(cleanerProperties).getAsString( "<div></div id=\"foo\">");
        assertEquals("<div></div>", output);
    }

    // public void testWhiteSpaceInTag() throws Exception {
    // String s =
    // "<html><body><table width=\"838\" cellpadding=\"5\" cellspacing=\"0\">\n"
    // +
    // "                <tbody>\n" +
    // "                <td width=\"704\"> </td>\n" +
    // "                </tr\n" +
    // "                ></tbody>< /table></bo dy>";
    // CleanerProperties cleanerProperties = new CleanerProperties();
    // cleanerProperties.setOmitHtmlEnvelope(false);
    // cleanerProperties.setOmitXmlDeclaration(true);
    // cleanerProperties.setUseEmptyElementTags(false);
    // String output = new
    // SimpleXmlSerializer().getXmlAsString(cleanerProperties, s, "UTF-8");
    // assertEquals("<html><head></head><body><table width=\"838\" cellpadding=\"5\" cellspacing=\"0\"><tbody><tr><td width=\"704\"> </td></tr></tbody></table></body></html>",output);
    // }
}
