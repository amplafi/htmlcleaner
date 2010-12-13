package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Testing node manipulation after cleaning.
 */
public class TagManipulationTest extends TestCase {

    private HtmlCleaner cleaner;
    private CleanerProperties props;

    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
        props = cleaner.getProperties();
    }

    public void testInnerHtml() throws XPatherException, IOException {
        TagNode node = cleaner.clean(new File("src/test/resources/test2.html"));
        cleaner.setInnerHtml((TagNode) (node.evaluateXPath("//table[1]")[0]), "<td>row1<td>row2<td>row3");
        assertEquals(node.evaluateXPath("//table[1]/tbody[1]/tr[1]/td").length, 3);
        assertEquals( cleaner.getInnerHtml((TagNode) (node.evaluateXPath("//table[1]")[0])),
                      "<tbody><tr><td>row1</td><td>row2</td><td>row3</td></tr></tbody>" );

        TagNode node9 = cleaner.clean(new File("src/test/resources/test9.html"));
        TagNode pNode = (TagNode) node9.evaluateXPath("//p[1]")[0];
        pNode.removeAllChildren();
        TagNode h3 = new TagNode("h3");
        pNode.addChild(h3);
        TagNode h2 = new TagNode("h2");
        TagNode h4 = new TagNode("h4");
        pNode.insertChildBefore(h3, h2);
        pNode.insertChildAfter(h3, h4);
        ContentNode testContent = new ContentNode("TEST BEFORE H3 AND AFTER H2");
        pNode.insertChild(1, testContent);
        pNode.addChild(new ContentNode("LAST_ONE"));

        assertTrue(pNode.getChildIndex(h4) == 3);

        props.setOmitXmlDeclaration(true);
        props.setNamespacesAware(false);
        String pNodeAsString = new CompactXmlSerializer(props).getAsString(pNode);
        pNodeAsString = pNodeAsString.replaceAll("\n", "");
        assertTrue( "<p><h2 />TEST BEFORE H3 AND AFTER H2<h3 /><h4 />LAST_ONE</p>".equals(pNodeAsString) );
    }

}