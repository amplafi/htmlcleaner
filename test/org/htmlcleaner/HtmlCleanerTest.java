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
    
    /** 
     * Test for #2901.
     * TODO: Fails due to some more random (but in body & innocent) whitespaces.  
     */
	public void testWhitespaceInHead() throws IOException {
		String initial = readFile("test/org/htmlcleaner/files/Real_1.html");
		String expected = readFile("test/org/htmlcleaner/files/Expected_1.html");
		assertCleaned(initial, expected);
	}
	
	/** 
	 * Mentioned in #2901 - we should eliminate the first <tr>
	 * TODO: Fails, instead of being removed, first <tr> is self closed and an additional one
	 * is added to the end of the table.
	 */
	public void testUselessTr() throws IOException {
		cleaner.getProperties().setAddNewlineToHeadAndBody(false);
		String start = "<html><head /><body><table>";
		String end = "</body></html>";
		assertCleaned(start + "<tr><tr><td>stuff</td></tr>" + end, 
				start + "<tbody><tr><td>stuff</td></tr></tbody></table>" + end);
	}
	
	/** 
	 * Mentioned in #2901 - yet another collapse test. 
	 */
	public void testUselessTr2() throws IOException {
		cleaner.getProperties().setAddNewlineToHeadAndBody(false);
		String start = "<html><head /><body><table>";
		String end = "</table></body></html>";
		assertCleaned(start + "<tr> </tr><tr><td>stuff</td></tr>" + end, 
				start + "<tbody><tr /><tr><td>stuff</td></tr></tbody>" + end);
	}	
	
	private void assertCleaned(String initial, String expected) throws IOException {
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
