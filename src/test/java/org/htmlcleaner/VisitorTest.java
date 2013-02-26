package org.htmlcleaner;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Testing XPath expressions against TagNodes results from cleaning process.
 */
public class VisitorTest extends TestCase {

    private TagNode node;
    private CleanerProperties props;

    protected void setUp() throws Exception {
        HtmlCleaner cleaner = new HtmlCleaner();
        props = cleaner.getProperties();
        node = cleaner.clean( new File("src/test/resources/test9.html") );
    }

    public void testNodeTraverse() throws IOException, XPatherException {
        final StringBuffer superstar = new StringBuffer();
        node.traverse(new TagNodeVisitor() {
            public boolean visit(TagNode parentNode, HtmlNode node) {
                if (node instanceof TagNode) {
                    TagNode tagNode = (TagNode) node;
                    String name = tagNode.getName();
                    if ( "p".equals(name) ) {
                        tagNode.removeAllChildren();
                    } else if ("h1".equals(name)) {
                        if ("superstar".equals(tagNode.getAttributeByName("id"))) {
                            superstar.append(tagNode.getText());
                            return false;
                        }
                    }
                } else if (node instanceof ContentNode) {
                } else if (node instanceof CommentNode) {
                }

                return true;
            }
        });

        assertEquals(node.evaluateXPath("//p[1]/*").length, 0);
        assertTrue("freestylo".equals(superstar.toString()));
        assertEquals(node.evaluateXPath("//p[2]/*").length, 1);
    }

}
