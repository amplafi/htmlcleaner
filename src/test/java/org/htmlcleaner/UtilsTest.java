package org.htmlcleaner;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
    public void testEscapeXml_transResCharsToNCR() {
        String res = Utils.escapeXml("1.&\"'<>", true, true, true, false, true);
        assertEquals("1.&#38;&#34;&#39;&#60;&#62;", res);
        
        res = Utils.escapeXml("2.&amp;&quot;&apos;&lt;&gt;", true, true, true, false, true);
        assertEquals("2.&#38;&#34;&#39;&#60;&#62;", res);
        
        res = Utils.escapeXml("1.&\"'<>", true, true, true, false, false);
        assertEquals("1.&amp;&quot;&apos;&lt;&gt;", res);
        
        res = Utils.escapeXml("2.&amp;&quot;&apos;&lt;&gt;", true, true, true, false, false);
        assertEquals("2.&amp;&quot;&apos;&lt;&gt;", res);
    }
}
