package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Testing HtmlCleaner constructors.
 */
public class ConstructorTest extends TestCase {

    public void testPrpertiesConstructor() throws XPatherException, IOException {
        CleanerProperties props = new CleanerProperties();
        props.setOmitComments(true);

        HtmlCleaner cleaner1 = new HtmlCleaner(props);
        TagNode node1 = cleaner1.clean("<a href=index.htm><b><!--COMMENT 1--><b>text text<body>");
        assertTrue( new SimpleXmlSerializer(props).getXmlAsString(node1).indexOf("<!--COMMENT 1-->") < 0 );

        HtmlCleaner cleaner2 = new HtmlCleaner(props);
        TagNode node2 = cleaner2.clean("<span href=index1.htm><b><!--COMMENT 2--><x>DDDD text<body>");
        assertTrue( new SimpleXmlSerializer(props).getXmlAsString(node2).indexOf("<!--COMMENT 2-->") < 0 );

        HtmlCleaner cleaner3 = new HtmlCleaner(props);
        props.setOmitComments(false);
        TagNode node3 = cleaner3.clean("<a href=index3.htm><b><!--COMMENT 3--><x>EEEEEEE text<body>");
        assertTrue( new SimpleXmlSerializer(props).getXmlAsString(node3).indexOf("<!--COMMENT 3-->") > 0 );
    }

}