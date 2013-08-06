package org.htmlcleaner;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;

/**
 * Testing HtmlCleaner constructors.
 */
public class ConstructorTest extends TestCase {

    public void testPropertiesConstructor() throws Exception {
        CleanerProperties props = new CleanerProperties();
        props.setOmitComments(true);

        HtmlCleaner cleaner1 = new HtmlCleaner(props);
        TagNode node1 = cleaner1.clean("<a href=index.htm><b><!--COMMENT 1--><b>text text<body>");
        assertTrue( new SimpleXmlSerializer(props).getAsString(node1).indexOf("<!--COMMENT 1-->") < 0 );

        HtmlCleaner cleaner2 = new HtmlCleaner(props);
        TagNode node2 = cleaner2.clean("<span href=index1.htm><b><!--COMMENT 2--><x>DDDD text<body>");
        assertTrue( new SimpleXmlSerializer(props).getAsString(node2).indexOf("<!--COMMENT 2-->") < 0 );

        HtmlCleaner cleaner3 = new HtmlCleaner(props);
        props.setOmitComments(false);
        TagNode node3 = cleaner3.clean("<a href=index3.htm><b><!--COMMENT 3--><x>EEEEEEE text<body>");
        assertTrue( new SimpleXmlSerializer(props).getAsString(node3).indexOf("<!--COMMENT 3-->") > 0 );

        TagNode node4 = cleaner3.clean( new ByteArrayInputStream( ("FIRST" + (char)0x2 + (char)0x3 + "SECOND").getBytes() ), "ASCII" );
        assertTrue( new CompactXmlSerializer(props).getAsString(node4).indexOf("FIRST  SECOND") >= 0 );

    }

}