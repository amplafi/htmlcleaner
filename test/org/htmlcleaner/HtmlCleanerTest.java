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
    
	public void testPage() throws IOException {
		File file = new File("test/org/htmlcleaner/files/Real_1.html");		
		StringBuffer content = Utils.readUrl(file.toURI().toURL(), "UTF-8");
        TagNode node = cleaner.clean( content.toString() );
        StringWriter writer = new StringWriter();
        serializer.serialize(node, writer);
        assertEquals(content.toString(), writer.toString());
	}    

}
