package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Testing tag transformations.
 */
public class TransformationTest extends TestCase {

    private HtmlCleaner cleaner;

    @Override
    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
    }

    public void test1() throws IOException {
        CleanerTransformations transformations = new CleanerTransformations();
        TagTransformation tagTransformation = new TagTransformation("strong", "span", false);
        tagTransformation.addAttributeTransformation("style", "font-weight:bold");
        transformations.addTransformation(tagTransformation);
        CleanerProperties props = cleaner.getProperties();
        props.setCleanerTransformations(transformations);
        props.setOmitXmlDeclaration(true);
        props.setAddNewlineToHeadAndBody(false);
        TagNode node = cleaner.clean("<div><strong>Mama</strong></div>");
        assertEquals(
                "<html><head /><body><div><span style=\"font-weight:bold\">Mama</span></div></body></html>",
                new CompactXmlSerializer(props).getAsString(node)
        );
    }

    public void test2() throws IOException {
        CleanerProperties props = cleaner.getProperties();

        CleanerTransformations transformations = props.getCleanerTransformations();

        TagTransformation t = new TagTransformation("blockquote");
        transformations.addTransformation(t);

        t = new TagTransformation("tags:bold", "td", false);
        t.addAttributeTransformation("style", "font-weight:bold;");
        transformations.addTransformation(t);

        t = new TagTransformation("table", "table", false);
        t.addAttributeTransformation("style", "${style};background:${bgcolor};border:solid ${border};");
        transformations.addTransformation(t);

        t = new TagTransformation("font", "span", true);
        t.addAttributeTransformation("style", "${style};font-family:${face};font-size:${size};color:${color};");
        t.addAttributeTransformation("face");
        t.addAttributeTransformation("size");
        t.addAttributeTransformation("color");
        t.addAttributeTransformation("name", "${face}_1");
        transformations.addTransformation(t);

        TagNode node = cleaner.clean( new File("src/test/resources/test8.html"), "UTF-8" );

        String xml = new PrettyXmlSerializer(props).getAsString(node);

        assertTrue("Shouldn't have blockquote in it "+xml, xml.indexOf("blockquote") < 0 );
        assertTrue( xml.indexOf("&quot;Hi there!&quot;") >= 0 );
        assertTrue( xml.indexOf("tags:bold") < 0 );
        assertTrue( xml.indexOf("<td style=\"font-weight:bold;\">This is BOLD text?!</td>") >= 0 );
        assertTrue( xml.indexOf("bgcolor=#DDDDDD") < 0 );
        assertTrue( xml.indexOf("style=\"padding:5\"") < 0 );
        assertTrue( xml.indexOf("<table style=\"padding:5;background:#DDDDDD;border:solid 2;\">") >= 0 );
        assertTrue( xml.indexOf("</font>") < 0 );
        assertTrue( xml.indexOf("color=red") < 0 );
        assertTrue( xml.indexOf("color=\"red\"") < 0 );
        assertTrue( xml.indexOf("size=16") < 0 );
        assertTrue( xml.indexOf("size=\"16\"") < 0 );
        assertTrue( xml.indexOf("face=\"Arial\"") < 0 );
        assertTrue( xml.indexOf("id=\"fnt_1\"") >= 0 );
        assertTrue( xml.indexOf("name=\"Arial_1\"") >= 0 );
        assertTrue( xml.indexOf("style=\";font-family:Arial;font-size:16;color:red;\"") >= 0 );
    }
    
    /**
     * 
     * @throws IOException
     */
    public void testGlobalTransformations() throws IOException {
        CleanerTransformations transformations = new CleanerTransformations();
        // no "on*" attributes
        AttributeTransformationPatternImpl attPattern = new AttributeTransformationPatternImpl(Pattern.compile("^\\s*on", Pattern.CASE_INSENSITIVE), null, null);
        transformations.addGlobalTransformation(attPattern);
        AttributeTransformationPatternImpl attPattern1 = new AttributeTransformationPatternImpl(null, Pattern.compile("^\\s*javascript:", Pattern.CASE_INSENSITIVE), null);
        transformations.addGlobalTransformation(attPattern1);
        CleanerProperties props = cleaner.getProperties();
        props.setCleanerTransformations(transformations);
        props.setOmitXmlDeclaration(true);
        props.setAddNewlineToHeadAndBody(false);
        TagNode node = cleaner.clean("<div onfoo=\"bar\" ONNot=\"\"><p bad=\" javascript:  \" class=\"javascript\" unknown=\"good\">Mama</p></div>");
        assertEquals(
                "<html><head /><body><div><p class=\"javascript\" unknown=\"good\">Mama</p></div></body></html>",
                new CompactXmlSerializer(props).getAsString(node)
        );
    }

}