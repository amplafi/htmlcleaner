package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;

/**
 * Testing tag transformations.
 */
public class TransformationTest extends TestCase {

    private HtmlCleaner cleaner;

    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
    }

    public void test1() throws XPatherException, IOException {
        CleanerTransformations transformations = new CleanerTransformations();
        TagTransformation tagTransformation = new TagTransformation("strong", "span", false);
        tagTransformation.addAttributeTransformation("style", "font-weight:bold");
        transformations.addTransformation(tagTransformation);
        cleaner.setTransformations(transformations);
        CleanerProperties props = cleaner.getProperties();
        props.setOmitXmlDeclaration(true);
        TagNode node = cleaner.clean("<div><strong>Mama</strong></div>");
        assertEquals(
                new CompactXmlSerializer(props).getXmlAsString(node),
                "<html><head /><body><div><span style=\"font-weight:bold\">Mama</span></div></body></html>"
        );
    }

    public void test2() throws IOException {
        CleanerProperties props = cleaner.getProperties();

        CleanerTransformations transformations = new CleanerTransformations();

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

        cleaner.setTransformations(transformations);
        
        TagNode node = cleaner.clean( new File("test/org/htmlcleaner/files/test8.html"), "UTF-8" );

        String xml = new PrettyXmlSerializer(props).getXmlAsString(node);

        assertTrue( xml.indexOf("blockquote") < 0 );
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

}