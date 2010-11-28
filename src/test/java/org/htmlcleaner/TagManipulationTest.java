package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Testing node manipulation after cleaning.
 */
public class TagManipulationTest extends TestCase {

    private HtmlCleaner cleaner;

    protected void setUp() throws Exception {
        cleaner = new HtmlCleaner();
    }

    public void testInnerHtml() throws XPatherException, IOException {
        TagNode node = cleaner.clean(new File("test/org/htmlcleaner/files/test2.html"));
        cleaner.setInnerHtml((TagNode) (node.evaluateXPath("//table[1]")[0]), "<td>row1<td>row2<td>row3");
        assertEquals(node.evaluateXPath("//table[1]/tbody[1]/tr[1]/td").length, 3);
        assertEquals( cleaner.getInnerHtml((TagNode) (node.evaluateXPath("//table[1]")[0])),
                      "<tbody><tr><td>row1</td><td>row2</td><td>row3</td></tr></tbody>" );
    }

}