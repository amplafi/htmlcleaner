package org.htmlcleaner;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import junit.framework.TestCase;

/**
 * Testing node manipulation after cleaning.
 * TODO String escaping tests should be moved to UtilsTest class [Eugene]
 * @author Eugene Sapozhnikov (blackorangebox@gmail.com)
 */
public class PropertiesTest extends TestCase {

    public void testPropertiesAdvancedXmlEscape() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);

        String xmlString;
        properties.setAdvancedXmlEscape(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div>&amp;&quot;&apos;&lt;&gt;</div>") >= 0);
        properties.setAdvancedXmlEscape(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString, xmlString.indexOf("<div>&amp;amp;&amp;quot;&amp;apos;&amp;lt;&amp;gt;</div>") >= 0);
    }

    public void testUseCdataForScriptAndStyle() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setNamespacesAware(false);
        properties.setAdvancedXmlEscape(false);
        properties.setUseCdataForScriptAndStyle(true);
        xmlString = getXmlString(cleaner, properties);
        String expected = "<script>" + XmlSerializer.SAFE_BEGIN_CDATA + "var x=y&&z;" + XmlSerializer.SAFE_END_CDATA
                + "</script>";
        assertTrue("looking for :\"" + expected + "\" in :\n" + xmlString, xmlString.indexOf(expected) >= 0);
        expected = "<style>" + XmlSerializer.SAFE_BEGIN_CDATA + ".test{font-size:10;}" + XmlSerializer.SAFE_END_CDATA
                + "</style>";
        assertTrue("looking for :\"" + expected + "\" in :\n" + xmlString, xmlString.indexOf(expected) >= 0);
        properties.setUseCdataForScriptAndStyle(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<script>var x=y&amp;&amp;z;</script>") >= 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<style>.test{font-size:10;}</style>") >= 0);
    }

    public void testTranslateSpecialEntities() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setTranslateSpecialEntities(true);
        String specialHtmlEntities = "<div>" + new String(new char[] { 244, 8240, 215, 376, 8364 }) + "</div>";
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf(specialHtmlEntities) >= 0);
        properties.setTranslateSpecialEntities(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf(specialHtmlEntities) < 0);
    }

    public void testRecognizeUnicodeChars() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        String unicodeCharString = "<div>" + new String(new char[] { 352, 8224, 8249 }) + "</div>";
        properties.setRecognizeUnicodeChars(true);
        assertTrue(getXmlString(cleaner, properties).indexOf(unicodeCharString) >= 0);
        properties.setRecognizeUnicodeChars(false);
        assertTrue(getXmlString(cleaner, properties).indexOf(unicodeCharString) < 0);
        assertTrue(getXmlString(cleaner, properties).indexOf("<div>&amp;#352;&amp;#8224;&amp;#8249;</div>") >= 0);
    }

    public void testOmitUnknownTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitUnknownTags(true);
        assertTrue(getXmlString(cleaner, properties).indexOf("<mytag>content of unknown tag</mytag>") < 0);
        assertTrue(getXmlString(cleaner, properties).indexOf("content of unknown tag") >= 0);
        properties.setOmitUnknownTags(false);
        assertTrue(getXmlString(cleaner, properties).indexOf("<mytag>content of unknown tag</mytag>") >= 0);
    }

    public void testTreatUnknownTagsAsContent() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitUnknownTags(false);
        properties.setTreatUnknownTagsAsContent(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("&lt;mytag&gt;content of unknown tag&lt;/mytag&gt;") >= 0);
        properties.setTreatUnknownTagsAsContent(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<mytag>content of unknown tag</mytag>") >= 0);
    }

    public void testNamespacesAware() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setNamespacesAware(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<html xmlns:my=\"my\">") >= 0);
        assertTrue(xmlString.indexOf("<my:tag id=\"xxx\">aaa</my:tag>") >= 0);
        properties.setNamespacesAware(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<html") >= 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<tag id=\"xxx\">aaa</tag>") >= 0);
    }

    public void testOmitDeprecatedTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDeprecatedTags(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<u>content of deprecated tag</u>") < 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("content of deprecated tag") >= 0);
        properties.setOmitDeprecatedTags(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<u>content of deprecated tag</u>") >= 0);
    }

    public void testTreatDeprecatedTagsAsContent() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDeprecatedTags(false);
        properties.setTreatDeprecatedTagsAsContent(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("&lt;u&gt;content of deprecated tag&lt;/u&gt;") >= 0);
        properties.setTreatDeprecatedTagsAsContent(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<u>content of deprecated tag</u>") >= 0);
    }

    /**
     * @throws IOException
     */
    public void testOmitComments() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);
        properties.setOmitComments(false);
        assertTrue(getXmlString(cleaner, properties).indexOf("<!--my comment-->") >= 0);
        properties.setOmitComments(true);
        assertTrue(getXmlString(cleaner, properties).indexOf("<!--my comment-->") < 0);
    }

    public void testUseEmptyElementTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        // Tag <a> connot be collapsed according to DefaultTagProvider
        properties.setUseEmptyElementTags(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<a href=\"index.php\" />") < 0);
        assertTrue(xmlString.indexOf("<a href=\"index.php\"></a>") >= 0);

        properties.setUseEmptyElementTags(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<a href=\"index.php\"></a>") >= 0);

        properties.setUseEmptyElementTags(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<br />") >= 0);

        xmlString = getXmlString(cleaner, properties);
        // jericho reports that td can not be empty. so we test on <tr/>
        // collapsing
        assertTrue(xmlString, xmlString.indexOf("<tr><td></td></tr><tr />") >= 0);
        properties.setUseEmptyElementTags(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<table><tbody><tr><td></td></tr><tr></tr></tbody></table>") >= 0);
    }

    public void testAllowMultiWordAttributes() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);
        properties.setUseEmptyElementTags(false);
        properties.setAllowMultiWordAttributes(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div att=\"a b c\">") < 0);
        assertTrue(xmlString.indexOf("<div att=\"a\" b=\"b\" c=\"c\">") >= 0);
        properties.setAllowMultiWordAttributes(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div att=\"a b c\">") >= 0);

        properties.setAllowHtmlInsideAttributes(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") >= 0);
        properties.setAllowHtmlInsideAttributes(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") < 0);
        assertTrue(xmlString.indexOf("<a title=\"\"><b>Title<b> is here&quot;&gt;LINK 1</b></b></a>") >= 0);

        properties.setIgnoreQuestAndExclam(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") < 0);
        assertTrue(xmlString.indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") < 0);
        properties.setIgnoreQuestAndExclam(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") >= 0);
        assertTrue(xmlString.indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") >= 0);

        properties.setNamespacesAware(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<html xmlns:my=\"my\">") >= 0);
        assertTrue(xmlString.indexOf("<my:tag id=\"xxx\">aaa</my:tag>") >= 0);
        properties.setNamespacesAware(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<html") >= 0);
        assertTrue(xmlString.indexOf("<tag id=\"xxx\">aaa</tag>") >= 0);
    }
    public void testAllowHtmlInsideAttributes() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setAllowHtmlInsideAttributes(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") >= 0 );
        properties.setAllowHtmlInsideAttributes(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<a title=\"\"><b>Title<b> is here&quot;&gt;LINK 1</b></b></a>") >= 0 );
    }
    public void testIgnoreQuestAndExclam() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setIgnoreQuestAndExclam(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") < 0 );
        properties.setIgnoreQuestAndExclam(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") >= 0 );
    }
    /**
     * @throws IOException
     */
    public void testComments() throws IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);
        properties.setOmitComments(false);
        assertTrue(getXmlString(cleaner, properties).indexOf("<!--my comment-->") >= 0);
        properties.setOmitComments(true);
        assertTrue(getXmlString(cleaner, properties).indexOf("<!--my comment-->") < 0);

        properties.setOmitComments(false);
        assertTrue(getXmlString(cleaner, properties).indexOf("<!-- comment with == - hyphen -->") >= 0);
        properties.setHyphenReplacementInComment("*");
        assertTrue(getXmlString(cleaner, properties).indexOf("<!-- comment with ** - hyphen -->") >= 0);
    }

    /**
     * @throws IOException
     */
    public void testOmitXmlDeclaration() throws IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);
        properties.setOmitXmlDeclaration(false);
        assertTrue(getXmlString(cleaner, properties).indexOf("<?xml version=\"1.0\"") >= 0);
        properties.setOmitXmlDeclaration(true);
        assertTrue(getXmlString(cleaner, properties).indexOf("<?xml version=\"1.0\"") < 0);
    }

    public void testOmitDoctypeDeclaration() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDoctypeDeclaration(false);
        assertTrue(getXmlString(cleaner, properties).indexOf(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") >= 0);
        properties.setOmitDoctypeDeclaration(true);
        assertTrue(getXmlString(cleaner, properties).indexOf(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") < 0);
    }

    /**
     * @throws IOException
     */
    public void testOmitHtmlEnvelope() throws IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);
        properties.setAddNewlineToHeadAndBody(false);
        String xmlString;
        properties.setOmitHtmlEnvelope(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<html><head>") < 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("</body></html>") < 0);
        properties.setOmitHtmlEnvelope(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString, xmlString.indexOf("<html><head>") >= 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString, xmlString.indexOf("</body></html>") >= 0);
    }

    public void testPruneProperties() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();

        properties.reset();
        properties.setPruneTags("div,mytag");
        String xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div") < 0);
        assertTrue(getXmlString(cleaner, properties).indexOf("<mytag") < 0);
        properties.setPruneTags("");
        properties.setAllowTags("html,body,div");
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div") >= 0);
        assertTrue(getXmlString(cleaner, properties).indexOf("<mytag") < 0);
    }

    public void testEmptyAttributesProperties() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();

        properties.reset();
        String xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<input checked=\"checked\" />") >= 0);
        properties.setBooleanAttributeValues("empty");
        assertTrue(getXmlString(cleaner, properties).indexOf("<input checked=\"\" />") >= 0);
        properties.setBooleanAttributeValues("true");
        assertTrue(getXmlString(cleaner, properties).indexOf("<input checked=\"true\" />") >= 0);
        properties.setBooleanAttributeValues("selft");
        assertTrue(getXmlString(cleaner, properties).indexOf("<input checked=\"checked\" />") >= 0);
    }

    private String getXmlString(HtmlCleaner cleaner, CleanerProperties properties) throws IOException {
        TagNode node = cleaner.clean(new File("src/test/resources/test4.html"), "UTF-8");
        String xmlString = new SimpleXmlSerializer(properties).getAsString(node);
        return xmlString;
    }

    public void testNbsp() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setTranslateSpecialEntities(false);
        properties.setOmitDoctypeDeclaration(false);
        properties.setOmitXmlDeclaration(true);
        properties.setAdvancedXmlEscape(true);
        properties.setAddNewlineToHeadAndBody(false);

        // test first when generating xml
        TagNode node = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"
                + "<div>&#x20;&amp;&quot;&apos;'&lt;&gt;&nbsp;&garbage;&</div>");
        SimpleXmlSerializer simpleXmlSerializer = new SimpleXmlSerializer(properties);
        String xmlString = simpleXmlSerializer.getAsString(node, "UTF-8");
        assertEquals("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"
                + "<html><head /><body><div> &amp;&quot;&apos;&apos;&lt;&gt;" + String.valueOf((char) 160)
                + "&amp;garbage;&amp;</div></body></html>", xmlString.trim());

        simpleXmlSerializer.setCreatingHtmlDom(true);
        // then test when generating html
        String domString = simpleXmlSerializer.getAsString(node, "UTF-8");
        assertEquals("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
        // "<html><head /><body><div> &amp;&quot;&#39;&#39;&lt;&gt;&nbsp;&amp;garbage;&amp;</div></body></html>",
                "<html><head /><body><div> &amp;&quot;''&lt;&gt;&nbsp;&amp;garbage;&amp;</div></body></html>",
                domString.trim());
    }

    /**
     * make sure that the unicode character has leading 'x'.
     * <ul>
     * <li>&#138A; is converted by FF to 3 characters: &#138; + 'A' + ';'</li>
     * <li>&#0x138A; is converted by FF to 6? 7? characters: &#0 'x'+'1'+'3'+
     * '8' + 'A' + ';' #0 is displayed kind of weird</li>
     * <li>&#x138A; is a single character</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testHexConversion() throws Exception {
        CleanerProperties properties = new CleanerProperties();
        properties.setOmitHtmlEnvelope(true);
        properties.setOmitXmlDeclaration(true);
        SimpleXmlSerializer simpleXmlSerializer = new SimpleXmlSerializer(properties);
        simpleXmlSerializer.setCreatingHtmlDom(false);

        String xmlString = simpleXmlSerializer.getAsString( "<div>&#138A;</div>");
        assertEquals("<div>"+new String(new char[] {138, 'A',';'})+"</div>", xmlString);
        xmlString = simpleXmlSerializer.getAsString( "<div>&#x138A;</div>");
        assertEquals("<div>"+new String(new char[] {0x138A})+"</div>", xmlString);
        properties.reset();

    }

    public void testPattern() {
        for (Object[] test : new Object[][] {
                new Object[] { "0x138A;", false, -1, -1, null, true, 0, 7, "x138A", true, 0, 1, "0" },
                new Object[] { "x138A;", true, 0, 6, "x138A", true, 0, 6, "x138A", false, -1, -1, null },
                new Object[] { "138;", false, -1, -1, null, false, -1, -1, null, true, 0, 4, "138" },
                new Object[] { "139", false, -1, -1, null, false, -1, -1, null, true, 0, 3, "139" },
                new Object[] { "x13A", true, 0, 4, "x13A", true, 0, 4, "x13A", false, -1, -1, null },
                new Object[] { "13F", false, -1, -1, null, false, -1, -1, null, true, 0, 2, "13" },
                new Object[] { "13", false, -1, -1, null, false, -1, -1, null, true, 0, 2, "13" },
                new Object[] { "X13AZ", true, 0, 4, "X13A", true, 0, 4, "X13A", false, -1, -1, null } }) {
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
        String output = new SimpleXmlSerializer(cleanerProperties).getAsString(
                "<h3><u><strong>President’s Message</strong></u><div> </h3>");
        assertEquals("<h3><u><strong>President’s Message</strong></u><div> </div></h3>", output);
    }

    private static final String HTML_COMMENT_OUT_BEGIN = "<html><head><script>";
    private static final String HTML_COMMENT_OUT_END = "</script></head><body></body></html>";
    private static final String SAMPLE_JS = "var x = ['foo','bar'];";
    private static final String COMMENT_START = "<!--";
    private static final String COMMENT_END = "-->";

    /**
     * Test conversion of former ( now bad practice ) of:
     * 
     * <pre>
     * &lt;style>&lt;!-- style info -->&lt;/style>
     * </pre>
     * 
     * into &lt;style>/(star)&lt;![CDATA[(star)/ style info
     * /(star)]]>(star)/&lt;/style>
     * 
     * Note: disabled because it doesn't test actual behavior
     * @throws IOException 
     */
    public void disabledTestConvertOldStyleComments() throws IOException {
        // TODO: May need additional flag to handle '<' inside of scripts
        // dontEscape() in xml serializer should not be triggered based on use
        // cdata
        // but dontEscape is used by subclasses -- need to investigate best
        // solution.
        // maybe o.k. to have the < > be translated. That is what original test
        // does.
        // but the ' should probably not be touched??
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = new CleanerProperties();
        properties.setOmitXmlDeclaration(true);
        properties.setUseCdataForScriptAndStyle(true);
        properties.setAddNewlineToHeadAndBody(false);
        // test for positive matches to old-style comment hacks
        for (String[] testData : new String[][] {
                // normal case - remove old-style comment out hack
                new String[] {
                        HTML_COMMENT_OUT_BEGIN + "//" + COMMENT_START + "\n" + SAMPLE_JS + "//" + COMMENT_END + "\n"
                                + HTML_COMMENT_OUT_END,
                        HTML_COMMENT_OUT_BEGIN + XmlSerializer.SAFE_BEGIN_CDATA + "\n" + SAMPLE_JS
                                + XmlSerializer.SAFE_END_CDATA + "\n" + HTML_COMMENT_OUT_END },
                // don't let random whitespace confuse things
                new String[] {
                        HTML_COMMENT_OUT_BEGIN + "\n\n\n\n" + "//" + "   \t" + COMMENT_START + "\n" + SAMPLE_JS
                                + "\n\n\n" + "//" + COMMENT_END + "\n\n\t\n" + HTML_COMMENT_OUT_END,
                        HTML_COMMENT_OUT_BEGIN + "\n\n\n\n" + XmlSerializer.SAFE_BEGIN_CDATA + "\n" + SAMPLE_JS
                                + "\n\n\n" + "//" + XmlSerializer.SAFE_END_CDATA + "\n\n\t\n" + HTML_COMMENT_OUT_END },

        }) {
            doTestConvertOldStyleComments(cleaner, properties, testData);
        }

        // test for false positives
        for (String[] testData : new String[][] {
        // make sure not to remove real comments
        new String[] {
                HTML_COMMENT_OUT_BEGIN + "//" + "an ordinary comment" + "\n" + SAMPLE_JS + "//" + "a final remark"
                        + HTML_COMMENT_OUT_END,
                HTML_COMMENT_OUT_BEGIN + XmlSerializer.SAFE_BEGIN_CDATA + "//" + "an ordinary comment" + "\n"
                        + SAMPLE_JS + "//" + "a final remark" + XmlSerializer.SAFE_END_CDATA + HTML_COMMENT_OUT_END }, }) {
            doTestConvertOldStyleComments(cleaner, properties, testData);
        }
    }

    /**
     * @param cleaner
     * @param properties
     * @param testData
     */
    private void doTestConvertOldStyleComments(HtmlCleaner cleaner, CleanerProperties properties, String[] testData)
            throws IOException {
        TagNode node = cleaner.clean(testData[0]);
        // test to make sure the no-op still works
        properties.setUseCdataForScriptAndStyle(false);
        String xmlString = new SimpleXmlSerializer(properties).getAsString(node);
        assertEquals(testData[0], xmlString);

        // now test actual
        properties.setUseCdataForScriptAndStyle(true);
        xmlString = new SimpleXmlSerializer(properties).getAsString(node);
        assertEquals(testData[1], xmlString);
    }

    public void testIgnoreClosingCData() throws IOException {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=utf-8\" /><link href=\"aswa.css\" type=\"text/css\" rel=\"stylesheet\" /><title>ASWA - Events</title>"
                + "<style type=\"text/css\">/*<![CDATA[*/\r\n"
                + "#ampmep_188 { }\r\n"
                + "/*]]>*/</style></head><body></body></html>";

        CleanerProperties properties = new CleanerProperties();
        properties.setOmitXmlDeclaration(true);
        properties.setUseCdataForScriptAndStyle(true);
        properties.setAddNewlineToHeadAndBody(false);
        HtmlCleaner cleaner = new HtmlCleaner(properties);
        TagNode node = cleaner.clean(html);
        properties.setUseCdataForScriptAndStyle(false);
        String xmlString = new SimpleXmlSerializer(properties).getAsString(node);
        assertEquals(html, xmlString);
    }

    public void testTransResCharsToNCR() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;

        properties.setNamespacesAware(false);
        properties.setAdvancedXmlEscape(true);
        properties.setTransResCharsToNCR(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div>1.&#38;&#34;&#39;&#60;&#62;</div>") >= 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div>2.&#38;&#34;&#39;&#60;&#62;</div>") >= 0);
        properties.setTransResCharsToNCR(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div>1.&amp;&quot;&apos;&lt;&gt;</div>") >= 0);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div>2.&amp;&quot;&apos;&lt;&gt;</div>") >= 0);
    }
}
