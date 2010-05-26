package org.htmlcleaner;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

public class HtmlCleanerTest extends TestCase {
	
	private HtmlCleaner cleaner;
	private XmlSerializer serializer;
	
    @Override
    protected void setUp() throws Exception {
        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setOmitXmlDeclaration(true);
        cleanerProperties.setOmitDoctypeDeclaration(false);
        cleanerProperties.setAdvancedXmlEscape(true);
        cleanerProperties.setTranslateSpecialEntities(false);
        cleanerProperties.setOmitComments(false);
        
        cleaner = new HtmlCleaner(cleanerProperties);
        serializer = new SimpleXmlSerializer(cleanerProperties);
    }
    
    /* #2901 */
	public void testWhitespaceInHead() throws IOException {
		String initial = readFile("test/org/htmlcleaner/files/Real_1.html");
		String expected = readFile("test/org/htmlcleaner/files/Expected_1.html");
        TagNode node = cleaner.clean(initial);
        StringWriter writer = new StringWriter();
        serializer.serialize(node, writer);
        assertEquals(expected, writer.toString());
	}
	
	private String readFile(String filename) throws IOException {
		File file = new File(filename);		
		StringBuffer content = Utils.readUrl(file.toURI().toURL(), "UTF-8");
		return content.toString();
	}

}
