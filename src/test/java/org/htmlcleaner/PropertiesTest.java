package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;

/**
 * Testing node manipulation after cleaning.
 */
public class PropertiesTest extends TestCase {

    public void testPropertiesAdvancedXmlEscape() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);

        String xmlString;
        properties.setAdvancedXmlEscape(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue(xmlString.indexOf("<div>&amp;&quot;&apos;&lt;&gt;</div>") >= 0 );
        properties.setAdvancedXmlEscape(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString, xmlString.indexOf("<div>&amp;amp;&amp;quot;&amp;apos;&amp;lt;&amp;gt;</div>") >= 0);
    }
    public void testTransResCharsToNCR() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;

        properties.setNamespacesAware(false);
        properties.setAdvancedXmlEscape(true);
        properties.setTransResCharsToNCR(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div>1.&#38;&#34;&#39;&#60;&#62;</div>") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div>2.&#38;&#34;&#39;&#60;&#62;</div>") >= 0 );
        properties.setTransResCharsToNCR(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div>1.&amp;&quot;&apos;&lt;&gt;</div>") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div>2.&amp;&quot;&apos;&lt;&gt;</div>") >= 0 );
    }
    public void testUseCdataForScriptAndStyle() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setNamespacesAware(false);
        properties.setAdvancedXmlEscape(false);
        properties.setUseCdataForScriptAndStyle(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<script><![CDATA[var x=y&&z;]]></script>") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<style><![CDATA[.test{font-size:10;}]]></style>") >= 0 );
        properties.setUseCdataForScriptAndStyle(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<script>var x=y&amp;&amp;z;</script>") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<style>.test{font-size:10;}</style>") >= 0 );
    }
    public void testTranslateSpecialEntities() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setTranslateSpecialEntities(true);
        String inputString = "<div>&ocirc;&permil;&times;&Yuml;&euro;</div>";
        String specialHtmlEntities = "<div>"+ new String(new char[] {244,8240, 215,376, 8364})+"</div>";
        TagNode node = cleaner.clean(inputString);
        xmlString = new SimpleXmlSerializer(properties).getAsString(node);
        assertTrue( xmlString.indexOf(specialHtmlEntities) >= 0 );
        properties.setTranslateSpecialEntities(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf(specialHtmlEntities) < 0 );
    }
    public void testRecognizeUnicodeChars() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        String unicodeCharString = "<div>"+ new String(new char[] {352, 8224,8249})+"</div>";
        properties.setRecognizeUnicodeChars(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf(unicodeCharString) >= 0 );
        properties.setRecognizeUnicodeChars(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf(unicodeCharString) < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div>&amp;#352;&amp;#8224;&amp;#8249;</div>") >= 0 );
    }
    public void testOmitUnknownTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitUnknownTags(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<mytag>content of unknown tag</mytag>") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("content of unknown tag") >= 0 );
        properties.setOmitUnknownTags(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<mytag>content of unknown tag</mytag>") >= 0 );
    }
    public void testTreatUnknownTagsAsContent() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitUnknownTags(false);
        properties.setTreatUnknownTagsAsContent(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("&lt;mytag&gt;content of unknown tag&lt;/mytag&gt;") >= 0 );
        properties.setTreatUnknownTagsAsContent(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<mytag>content of unknown tag</mytag>") >= 0 );
    }
    public void testOmitDeprecatedTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDeprecatedTags(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<u>content of deprecated tag</u>") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("content of deprecated tag") >= 0 );
        properties.setOmitDeprecatedTags(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<u>content of deprecated tag</u>") >= 0 );
    }
    public void testTreatDeprecatedTagsAsContent() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDeprecatedTags(false);
        properties.setTreatDeprecatedTagsAsContent(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("&lt;u&gt;content of deprecated tag&lt;/u&gt;") >= 0 );
        properties.setTreatDeprecatedTagsAsContent(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<u>content of deprecated tag</u>") >= 0 );
    }
    public void testOmitComments() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitComments(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<!--my comment-->") >= 0 );
        properties.setOmitComments(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<!--my comment-->") < 0 );
    }
    public void testOmitXmlDeclaration() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitXmlDeclaration(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<?xml version=\"1.0\"") >= 0 );
        properties.setOmitXmlDeclaration(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<?xml version=\"1.0\"") < 0 );
    }
    public void testOmitDoctypeDeclaration() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDoctypeDeclaration(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") >= 0 );
        properties.setOmitDoctypeDeclaration(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") < 0 );
    }
    public void testOmitHtmlEnvelope() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitHtmlEnvelope(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<html><head>") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("</body></html>") < 0 );
        properties.setOmitHtmlEnvelope(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<html><head>") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("</body></html>") >= 0 );
    }
    public void testUseEmptyElementTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);
        properties.setUseEmptyElementTags(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<a href=\"index.php\" />") >= 0 );
        properties.setUseEmptyElementTags(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<a href=\"index.php\"></a>") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<br />") >= 0 );

        properties.setUseEmptyElementTags(true);
        xmlString = getXmlString(cleaner, properties);
        // jericho reports that td can not be empty. so we test on <tr/> collapsing
        // TODO : Need to fix.
//        assertTrue(xmlString, xmlString.indexOf("<tr><td></td></tr><tr />") >= 0);
        properties.setUseEmptyElementTags(false);
        xmlString = getXmlString(cleaner, properties);
//        assertTrue(xmlString.indexOf("<table><tbody><tr><td></td></tr><tr></tr></tbody></table>") >= 0);
    }
    public void testAllowMultiWordAttributes() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);
        properties.setUseEmptyElementTags(false);

        properties.setAllowMultiWordAttributes(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div att=\"a b c\">") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div att=\"a\" b=\"b\" c=\"c\">") >= 0 );
        properties.setAllowMultiWordAttributes(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div att=\"a b c\">") >= 0 );
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
    public void testNamespacesAware() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setNamespacesAware(true);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<my:tag id=\"xxx\" xmlns:my=\"my\">aaa</my:tag>") >= 0 );
        properties.setNamespacesAware(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<html") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<tag id=\"xxx\">aaa</tag>") >= 0 );
    }
    public void testComments() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;
        properties.setAdvancedXmlEscape(false);

        properties.setOmitComments(false);
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<!-- comment with == - hyphen -->") >= 0 );
        properties.setHyphenReplacementInComment("*");
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<!-- comment with ** - hyphen -->") >= 0 );
    }
    public void testPruneProperties() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;

        properties.setPruneTags("div,mytag");
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div") < 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<mytag") < 0 );
        properties.setPruneTags("");
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<div") >= 0 );
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<mytag") >= 0 );
    }
    public void testEmptyAttributesProperties() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        String xmlString;

        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<input checked=\"checked\" />") >= 0 );
        properties.setBooleanAttributeValues("empty");
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<input checked=\"\" />") >= 0 );
        properties.setBooleanAttributeValues("true");
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<input checked=\"true\" />") >= 0 );
        properties.setBooleanAttributeValues("selft");
        xmlString = getXmlString(cleaner, properties);
        assertTrue( xmlString.indexOf("<input checked=\"checked\" />") >= 0 );
    }

    private String getXmlString(HtmlCleaner cleaner, CleanerProperties properties) throws IOException {
        TagNode node = cleaner.clean( new File("src/test/resources/test4.html"), "UTF-8" );
        return new SimpleXmlSerializer(properties).getAsString(node);
    }

}
