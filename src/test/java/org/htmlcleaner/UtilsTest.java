package org.htmlcleaner;

import junit.framework.TestCase;

/**
 * 
 * @author Eugene Sapozhnikov (blackorangebox@gmail.com)
 *
 */
public class UtilsTest extends TestCase {
    public void testEscapeXml_transResCharsToNCR() {
        String res = Utils.escapeXml("1.&\"'<>", true, true, true, false, true, false);
        assertEquals("1.&#38;&#34;&#39;&#60;&#62;", res);
        
        res = Utils.escapeXml("2.&amp;&quot;&apos;&lt;&gt;", true, true, true, false, true, false);
        assertEquals("2.&#38;&#34;&#39;&#60;&#62;", res);
        
        res = Utils.escapeXml("1.&\"'<>", true, true, true, false, false, false);
        assertEquals("1.&amp;&quot;&apos;&lt;&gt;", res);
        
        res = Utils.escapeXml("2.&amp;&quot;&apos;&lt;&gt;", true, true, true, false, false, false);
        assertEquals("2.&amp;&quot;&apos;&lt;&gt;", res);
    }
    
    public void testEscapeXml_recognizeUnicodeChars() {
        String res = Utils.escapeXml("[&alpha;][&eacute;][&oline;]", true, false, true, false, false, false);
        assertEquals("[&#945;][&#233;][&#8254;]", res);
        
        res = Utils.escapeXml("[&alpha;][&eacute;][&oline;][&#931;]", true, true, true, false, false, false);
        assertEquals("[α][é][‾][Σ]", res);
    }
    
    public void testEscapeXml_transSpecialEntitiesToNCR_withHex() {
        String res = Utils.escapeXml("&#x27;&#xa1;", true, false, true, false, false, true);
        assertEquals("&#x27;&#xa1;", res);   
        
        res = Utils.escapeXml("&#39;&#161;", true, false, true, false, false, true);
        assertEquals("&#39;&#161;", res);   
        
        res = Utils.escapeXml("&#x27;&#xa1;", true, false, true, false, false, false);
        assertEquals("&apos;¡", res);   
    }
}
