package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;
import java.util.regex.Matcher;

/**
 * Testing node manipulation after cleaning.
 */
public class PropertiesTest extends TestCase {

    private HtmlCleaner cleaner;
    private CleanerProperties properties;

    @Override
    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
        properties = cleaner.getProperties();
    }

    public void testProperties() throws Exception {
        properties.setNamespacesAware(false);

        String xmlString;
        properties.setAdvancedXmlEscape(true);
        xmlString = getXmlString();
        assertTrue(xmlString.indexOf("<div>&amp;&quot;&apos;&lt;&gt;</div>") >= 0 );
        properties.setAdvancedXmlEscape(false);
        xmlString = getXmlString();
        assertTrue( xmlString, xmlString.indexOf("<div>&amp;amp;&amp;quot;&amp;apos;&amp;lt;&amp;gt;</div>") >= 0);

        properties.setUseCdataForScriptAndStyle(true);
        xmlString = getXmlString();
        String expected = "<script>"+XmlSerializer.SAFE_BEGIN_CDATA+"var x=y&&z;"+XmlSerializer.SAFE_END_CDATA+"</script>";
        assertTrue("looking for :\""+expected+"\" in :\n"+ xmlString,  xmlString.indexOf(expected) >= 0 );
        expected = "<style>"+XmlSerializer.SAFE_BEGIN_CDATA+".test{font-size:10;}"+XmlSerializer.SAFE_END_CDATA+"</style>";
        assertTrue("looking for :\""+expected+"\" in :\n"+ xmlString, xmlString.indexOf(expected) >= 0);
        assertTrue( xmlString.indexOf("<script></script>") >= 0 );
        assertTrue( xmlString.indexOf("<style></style>") >= 0 );
        properties.setUseCdataForScriptAndStyle(false);
        assertTrue( getXmlString().indexOf("<script>var x=y&amp;&amp;z;</script>") >= 0 );
        assertTrue( getXmlString().indexOf("<style>.test{font-size:10;}</style>") >= 0 );

        properties.setTranslateSpecialEntities(true);
        String specialHtmlEntities = "<div>"+ new String(new char[] {244,8240, 215,376, 8364})+"</div>";
        xmlString = getXmlString();
        assertTrue( xmlString.indexOf(specialHtmlEntities) >= 0 );
        properties.setTranslateSpecialEntities(false);
        assertTrue( getXmlString().indexOf(specialHtmlEntities) < 0 );

        String unicodeCharString = "<div>"+ new String(new char[] {352, 8224,8249})+"</div>";
        properties.setRecognizeUnicodeChars(true);
        assertTrue( getXmlString().indexOf(unicodeCharString) >= 0 );
        properties.setRecognizeUnicodeChars(false);
        assertTrue( getXmlString().indexOf(unicodeCharString) < 0 );
        assertTrue( getXmlString().indexOf("<div>&amp;#352;&amp;#8224;&amp;#8249;</div>") >= 0 );

        properties.setOmitUnknownTags(true);
        assertTrue( getXmlString().indexOf("<mytag>content of unknown tag</mytag>") < 0 );
        assertTrue( getXmlString().indexOf("content of unknown tag") >= 0 );
        properties.setOmitUnknownTags(false);
        assertTrue( getXmlString().indexOf("<mytag>content of unknown tag</mytag>") >= 0 );

        properties.setOmitUnknownTags(false);
        properties.setTreatUnknownTagsAsContent(true);
        assertTrue( getXmlString().indexOf("&lt;mytag&gt;content of unknown tag&lt;/mytag&gt;") >= 0 );
        properties.setTreatUnknownTagsAsContent(false);
        assertTrue( getXmlString().indexOf("<mytag>content of unknown tag</mytag>") >= 0 );

        properties.setOmitDeprecatedTags(true);
        assertTrue( getXmlString().indexOf("<u>content of deprecated tag</u>") < 0 );
        assertTrue( getXmlString().indexOf("content of deprecated tag") >= 0 );
        properties.setOmitDeprecatedTags(false);
        assertTrue( getXmlString().indexOf("<u>content of deprecated tag</u>") >= 0 );

        properties.setOmitDeprecatedTags(false);
        properties.setTreatDeprecatedTagsAsContent(true);
        assertTrue( getXmlString().indexOf("&lt;u&gt;content of deprecated tag&lt;/u&gt;") >= 0 );
        properties.setTreatDeprecatedTagsAsContent(false);
        assertTrue( getXmlString().indexOf("<u>content of deprecated tag</u>") >= 0 );

        properties.setOmitComments(false);
        assertTrue( getXmlString().indexOf("<!--my comment-->") >= 0 );
        properties.setOmitComments(true);
        assertTrue( getXmlString().indexOf("<!--my comment-->") < 0 );

        properties.setOmitXmlDeclaration(false);
        assertTrue( getXmlString().indexOf("<?xml version=\"1.0\"") >= 0 );
        properties.setOmitXmlDeclaration(true);
        assertTrue( getXmlString().indexOf("<?xml version=\"1.0\"") < 0 );

        properties.setOmitDoctypeDeclaration(false);
        assertTrue( getXmlString().indexOf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") >= 0 );
        properties.setOmitDoctypeDeclaration(true);
        assertTrue( getXmlString().indexOf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") < 0 );

        properties.setOmitHtmlEnvelope(true);
        assertTrue( getXmlString().indexOf("<html><head>") < 0 );
        assertTrue( getXmlString().indexOf("</body></html>") < 0 );
        properties.setOmitHtmlEnvelope(false);
        assertTrue( getXmlString().indexOf("<html><head>") >= 0 );
        assertTrue( getXmlString().indexOf("</body></html>") >= 0 );

        properties.setUseEmptyElementTags(true);
        xmlString = getXmlString();
        assertTrue( xmlString.indexOf("<a href=\"index.php\"></a>") >= 0 );
        assertTrue(xmlString, xmlString.indexOf("<tr><td /></tr><tr />") >= 0);
        properties.setUseEmptyElementTags(false);
        xmlString = getXmlString();
        assertTrue(xmlString.indexOf("<table><tbody><tr><td></td></tr><tr></tr></tbody></table>") >= 0);
        assertTrue( xmlString.indexOf("<a href=\"index.php\"></a>") >= 0 );
        assertTrue( xmlString.indexOf("<br />") >= 0 );

        properties.setAllowMultiWordAttributes(false);
        assertTrue( getXmlString().indexOf("<div att=\"a b c\">") < 0 );
        assertTrue( getXmlString().indexOf("<div att=\"a\" b=\"b\" c=\"c\">") >= 0 );
        properties.setAllowMultiWordAttributes(true);
        assertTrue( getXmlString().indexOf("<div att=\"a b c\">") >= 0 );

        properties.setAllowHtmlInsideAttributes(true);
        assertTrue( getXmlString().indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") >= 0 );
        properties.setAllowHtmlInsideAttributes(false);
        assertTrue( getXmlString().indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") < 0 );
        assertTrue( getXmlString().indexOf("<a title=\"\"><b>Title<b> is here&quot;&gt;LINK 1</b></b></a>") >= 0 );

        properties.setIgnoreQuestAndExclam(true);
        assertTrue( getXmlString().indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") < 0 );
        assertTrue( getXmlString().indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") < 0 );
        properties.setIgnoreQuestAndExclam(false);
        assertTrue( getXmlString().indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") >= 0 );
        assertTrue( getXmlString().indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") >= 0 );

        properties.setNamespacesAware(true);
        assertTrue( getXmlString().indexOf("<html xmlns:my=\"my\">") >= 0 );
        assertTrue( getXmlString().indexOf("<my:tag id=\"xxx\">aaa</my:tag>") >= 0 );
        properties.setNamespacesAware(false);
        assertTrue( getXmlString().indexOf("<html") >= 0 );
        assertTrue( getXmlString().indexOf("<tag id=\"xxx\">aaa</tag>") >= 0 );

        properties.setOmitComments(false);
        assertTrue( getXmlString().indexOf("<!-- comment with == - hyphen -->") >= 0 );
        properties.setHyphenReplacementInComment("*");
        assertTrue( getXmlString().indexOf("<!-- comment with ** - hyphen -->") >= 0 );
    }
    public void testPruneProperties() throws Exception {
        properties.reset();
        properties.setPruneTags("div,mytag");
        String xmlString = getXmlString();
        assertTrue( xmlString.indexOf("<div") < 0 );
        assertTrue( getXmlString().indexOf("<mytag") < 0 );
        properties.setPruneTags("");
        properties.setAllowTags("html,body,div");
        xmlString = getXmlString();
        assertTrue( xmlString.indexOf("<div") >= 0 );
        assertTrue( getXmlString().indexOf("<mytag") < 0 );
    }
    public void testEmptyAttributesProperties() throws Exception {
        properties.reset();
        String xmlString = getXmlString();
        assertTrue( xmlString.indexOf("<input checked=\"checked\" />") >= 0 );
        properties.setBooleanAttributeValues("empty");
        assertTrue( getXmlString().indexOf("<input checked=\"\" />") >= 0 );
        properties.setBooleanAttributeValues("true");
        assertTrue( getXmlString().indexOf("<input checked=\"true\" />") >= 0 );
        properties.setBooleanAttributeValues("selft");
        assertTrue( getXmlString().indexOf("<input checked=\"checked\" />") >= 0 );
    }

    private String getXmlString() throws IOException {
        TagNode node = cleaner.clean( new File("test/org/htmlcleaner/files/test4.html"), "UTF-8" );
        String xmlString = new SimpleXmlSerializer(properties).getXmlAsString(node);
        return xmlString;
    }

    public void testNbsp() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setTranslateSpecialEntities(false);
        properties.setOmitDoctypeDeclaration(false);
        properties.setOmitXmlDeclaration(true);
        properties.setAdvancedXmlEscape(true);

        // test first when generating xml
        TagNode node = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
        		"<div>&#x20;&amp;&quot;&apos;'&lt;&gt;&nbsp;&garbage;&</div>");
        SimpleXmlSerializer simpleXmlSerializer = new SimpleXmlSerializer(properties);
        String xmlString = simpleXmlSerializer.getXmlAsString(node, "UTF-8" );
        assertEquals("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                "<html><head /><body><div> &amp;&quot;&apos;&apos;&lt;&gt;"+String.valueOf((char)160)+"&amp;garbage;&amp;</div></body></html>", xmlString.trim());

        simpleXmlSerializer.setCreatingHtmlDom(true);
        // then test when generating html
        String domString = simpleXmlSerializer.getXmlAsString(node, "UTF-8" );
        assertEquals("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
//            "<html><head /><body><div> &amp;&quot;&#39;&#39;&lt;&gt;&nbsp;&amp;garbage;&amp;</div></body></html>", 
            "<html><head /><body><div> &amp;&quot;''&lt;&gt;&nbsp;&amp;garbage;&amp;</div></body></html>", 
                domString.trim());
    }

    /**
     * make sure that the unicode character has leading 'x'.
     * <ul>
     * <li>&#138A; is converted by FF to 3 characters: &#138; + 'A' + ';'</li>
     * <li>&#0x138A; is converted by FF to 6? 7? characters: &#0 'x'+'1'+'3'+ '8' + 'A' + ';'
     * #0 is displayed kind of weird</li>
     * <li>&#x138A; is a single character</li>
     * </ul>
     * @throws Exception
     */
    public void testHexConversion() throws Exception {
        SimpleXmlSerializer simpleXmlSerializer = new SimpleXmlSerializer();
        simpleXmlSerializer.setCreatingHtmlDom(false);
        CleanerProperties properties = new CleanerProperties();
        properties.setOmitHtmlEnvelope(true);
        properties.setOmitXmlDeclaration(true);

        String xmlString = simpleXmlSerializer.getXmlAsString(properties, "<div>&#138A;</div>", "UTF-8");
        assertEquals("<div>"+new String(new char[] {138, 'A',';'})+"</div>", xmlString);
        xmlString = simpleXmlSerializer.getXmlAsString(properties, "<div>&#x138A;</div>", "UTF-8");
        assertEquals("<div>"+new String(new char[] {0x138A})+"</div>", xmlString);
        properties.reset();

    }

    public void testPattern() {
        for(Object[] test : new Object[][] {
            new Object[] { "0x138A;", false, -1, -1, null,
                                      true, 0, 7, "x138A",
                                      true, 0, 1, "0"},
            new Object[] {"x138A;", true, 0, 6, "x138A",
                                    true, 0, 6, "x138A",
                                    false, -1,-1, null},
            new Object[] {"138;", false, -1, -1, null,
                                  false, -1, -1, null,
                                  true, 0, 4, "138"},
            new Object[] {"139", false, -1, -1, null,
                                 false, -1, -1, null,
                                 true, 0, 3, "139"},
            new Object[] {"x13A", true, 0, 4, "x13A",
                                    true, 0, 4, "x13A",
                                    false, -1, -1, null},
            new Object[] {"13F", false, -1, -1, null,
                                 false, -1, -1, null,
                                 true, 0, 2, "13"},
            new Object[] {"13", false, -1, -1, null,
                                false, -1, -1, null,
                                true, 0, 2, "13"},
            new Object[] {"X13AZ", true, 0, 4, "X13A",
                                   true, 0, 4, "X13A",
                                   false, -1, -1, null}}) {
            int i = 0;
            String input = (String) test[i++];
            boolean strict = (Boolean) test[i++];
            int sstart = (Integer) test[i++];
            int send = (Integer) test[i++];
            String sgroup = (String) test[i++];
            boolean relaxed = (Boolean) test[i++];
            int rstart = (Integer) test[i++];
            int rend = (Integer) test[i++];
            String rgroup = (String) test[i++];
            boolean decimal = (Boolean) test[i++];
            int dstart = (Integer) test[i++];
            int dend = (Integer) test[i++];
            String dgroup = (String) test[i++];
            Matcher m = Utils.HEX_STRICT.matcher(input);
            boolean actual = m.find();
            assertEquals(input, strict, actual);
            if (actual) {
                assertEquals(input + " strict start ", sstart, m.start());
                assertEquals(input + " strict end ", send, m.end());
                assertEquals(input + " strict group ", sgroup, m.group(1));
            }
            m = Utils.HEX_RELAXED.matcher(input);
            actual = m.find();
            assertEquals(input, relaxed, actual);
            if (actual) {
                assertEquals(input + " relaxed start ", rstart, m.start());
                assertEquals(input + " relaxed end ", rend, m.end());
                assertEquals(input + " relaxed group ", rgroup, m.group(1));
            }
            m = Utils.DECIMAL.matcher(input);
            actual = m.find();
            assertEquals(input, decimal, actual);
            if (actual) {
                assertEquals(input + " decimal start ", dstart, m.start());
                assertEquals(input + " decimal end ", dend, m.end());
                assertEquals(input + " decimal group ", dgroup, m.group(1));
            }
        }
    }
    
    public void testConvertUnicode() throws Exception {
        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setOmitHtmlEnvelope(true);
        cleanerProperties.setOmitXmlDeclaration(true);
        cleanerProperties.setUseEmptyElementTags(false);
        // right tick is special unicode character 8217
        String output = new SimpleXmlSerializer().getXmlAsString(cleanerProperties, "<h3><u><strong>President’s Message</strong></u><div> </h3>", "UTF-8");
        assertEquals("<h3><u><strong>President’s Message</strong></u><div> </div></h3>", output);
    }
    
    /**
     * Test conversion of former ( now bad practice ) of:
     * <pre>
     * &lt;style>&lt;!-- style info -->&lt;/style>
     * </pre>
     * into
     * 
     */
    //public void testConvertOldStyleComments() {
        
    //}

}