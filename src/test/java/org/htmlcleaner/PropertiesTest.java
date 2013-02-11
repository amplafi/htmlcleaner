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

        properties.setNamespacesAware(false);
        properties.setAdvancedXmlEscape(true);
        properties.setTransResCharsToNCR(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<div>1.&#38;&#34;&#39;&#60;&#62;</div>") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<div>2.&#38;&#34;&#39;&#60;&#62;</div>") >= 0 );
        properties.setTransResCharsToNCR(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<div>1.&amp;&quot;&apos;&lt;&gt;</div>") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<div>2.&amp;&quot;&apos;&lt;&gt;</div>") >= 0 );
    }
    public void testUseCdataForScriptAndStyle() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setNamespacesAware(false);
        properties.setAdvancedXmlEscape(false);
        properties.setUseCdataForScriptAndStyle(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<script><![CDATA[var x=y&&z;]]></script>") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<style><![CDATA[.test{font-size:10;}]]></style>") >= 0 );
        properties.setUseCdataForScriptAndStyle(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<script>var x=y&amp;&amp;z;</script>") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<style>.test{font-size:10;}</style>") >= 0 );
    }
    public void testTranslateSpecialEntities() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setTranslateSpecialEntities(true);
        String specialHtmlEntities = "<div>"+ new String(new char[] {244,8240, 215,376, 8364})+"</div>";
        assertTrue( getXmlString(cleaner, properties).indexOf(specialHtmlEntities) >= 0 );
        properties.setTranslateSpecialEntities(false);
        assertTrue( getXmlString(cleaner, properties).indexOf(specialHtmlEntities) < 0 );
    }
    public void testRecognizeUnicodeChars() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        String unicodeCharString = "<div>"+ new String(new char[] {352, 8224,8249})+"</div>";
        properties.setRecognizeUnicodeChars(true);
        assertTrue( getXmlString(cleaner, properties).indexOf(unicodeCharString) >= 0 );
        properties.setRecognizeUnicodeChars(false);
        assertTrue( getXmlString(cleaner, properties).indexOf(unicodeCharString) < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<div>&amp;#352;&amp;#8224;&amp;#8249;</div>") >= 0 );
    }
    public void testOmitUnknownTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitUnknownTags(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<mytag>content of unknown tag</mytag>") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("content of unknown tag") >= 0 );
        properties.setOmitUnknownTags(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<mytag>content of unknown tag</mytag>") >= 0 );
    }
    public void testTreatUnknownTagsAsContent() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitUnknownTags(false);
        properties.setTreatUnknownTagsAsContent(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("&lt;mytag&gt;content of unknown tag&lt;/mytag&gt;") >= 0 );
        properties.setTreatUnknownTagsAsContent(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<mytag>content of unknown tag</mytag>") >= 0 );
    }
    public void testOmitDeprecatedTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDeprecatedTags(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<u>content of deprecated tag</u>") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("content of deprecated tag") >= 0 );
        properties.setOmitDeprecatedTags(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<u>content of deprecated tag</u>") >= 0 );
    }
    public void testTreatDeprecatedTagsAsContent() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDeprecatedTags(false);
        properties.setTreatDeprecatedTagsAsContent(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("&lt;u&gt;content of deprecated tag&lt;/u&gt;") >= 0 );
        properties.setTreatDeprecatedTagsAsContent(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<u>content of deprecated tag</u>") >= 0 );
    }
    public void testOmitComments() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitComments(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<!--my comment-->") >= 0 );
        properties.setOmitComments(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<!--my comment-->") < 0 );
    }
    public void testOmitXmlDeclaration() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitXmlDeclaration(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<?xml version=\"1.0\"") >= 0 );
        properties.setOmitXmlDeclaration(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<?xml version=\"1.0\"") < 0 );
    }
    public void testOmitDoctypeDeclaration() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitDoctypeDeclaration(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") >= 0 );
        properties.setOmitDoctypeDeclaration(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">") < 0 );
    }
    public void testOmitHtmlEnvelope() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitHtmlEnvelope(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<html><head>") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("</body></html>") < 0 );
        properties.setOmitHtmlEnvelope(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<html><head>") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("</body></html>") >= 0 );
    }
    public void testUseEmptyElementTags() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setUseEmptyElementTags(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<a href=\"index.php\" />") >= 0 );
        properties.setUseEmptyElementTags(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<a href=\"index.php\"></a>") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<br />") >= 0 );
    }
    public void testAllowMultiWordAttributes() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);
        properties.setUseEmptyElementTags(false);

        properties.setAllowMultiWordAttributes(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<div att=\"a b c\">") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<div att=\"a\" b=\"b\" c=\"c\">") >= 0 );
        properties.setAllowMultiWordAttributes(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<div att=\"a b c\">") >= 0 );
    }
    public void testAllowHtmlInsideAttributes() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setAllowHtmlInsideAttributes(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") >= 0 );
        properties.setAllowHtmlInsideAttributes(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<a title=\"&lt;b&gt;Title&lt;b&gt; is here\">LINK 1</a>") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<a title=\"\"><b>Title<b> is here&quot;&gt;LINK 1</b></b></a>") >= 0 );
    }
    public void testIgnoreQuestAndExclam() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setIgnoreQuestAndExclam(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") < 0 );
        properties.setIgnoreQuestAndExclam(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("&lt;!INSTRUCTION1 id=&quot;aaa&quot;&gt;") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("&lt;?INSTRUCTION2 id=&quot;bbb&quot;&gt;") >= 0 );
    }
    public void testNamespacesAware() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setNamespacesAware(true);
        assertTrue( getXmlString(cleaner, properties).indexOf("<my:tag id=\"xxx\" xmlns:my=\"my\">aaa</my:tag>") >= 0 );
        properties.setNamespacesAware(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<html") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<tag id=\"xxx\">aaa</tag>") >= 0 );
    }
    public void testComments() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setAdvancedXmlEscape(false);

        properties.setOmitComments(false);
        assertTrue( getXmlString(cleaner, properties).indexOf("<!-- comment with == - hyphen -->") >= 0 );
        properties.setHyphenReplacementInComment("*");
        assertTrue( getXmlString(cleaner, properties).indexOf("<!-- comment with ** - hyphen -->") >= 0 );
    }
    public void testPruneProperties() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();

        properties.setPruneTags("div,mytag");
        assertTrue( getXmlString(cleaner, properties).indexOf("<div") < 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<mytag") < 0 );
        properties.setPruneTags("");
        assertTrue( getXmlString(cleaner, properties).indexOf("<div") >= 0 );
        assertTrue( getXmlString(cleaner, properties).indexOf("<mytag") >= 0 );
    }
    public void testEmptyAttributesProperties() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();

        assertTrue( getXmlString(cleaner, properties).indexOf("<input checked=\"checked\" />") >= 0 );
        properties.setBooleanAttributeValues("empty");
        assertTrue( getXmlString(cleaner, properties).indexOf("<input checked=\"\" />") >= 0 );
        properties.setBooleanAttributeValues("true");
        assertTrue( getXmlString(cleaner, properties).indexOf("<input checked=\"true\" />") >= 0 );
        properties.setBooleanAttributeValues("selft");
        assertTrue( getXmlString(cleaner, properties).indexOf("<input checked=\"checked\" />") >= 0 );
    }

    private String getXmlString(HtmlCleaner cleaner, CleanerProperties properties) throws IOException {
        TagNode node = cleaner.clean( new File("src/test/resources/test4.html"), "UTF-8" );
        return new SimpleXmlSerializer(properties).getAsString(node);
    }

}