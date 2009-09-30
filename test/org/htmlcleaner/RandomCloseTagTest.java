package org.htmlcleaner;

import junit.framework.TestCase;

public class RandomCloseTagTest extends TestCase {
	
	public void testRandomCloseTagsRemoved(){
		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties properties = cleaner.getProperties();
		properties.setOmitHtmlEnvelope(true);
		properties.setOmitXmlDeclaration(true);
		SimpleXmlSerializer serializer = new SimpleXmlSerializer(properties);
		TagNode cleaned = cleaner.clean("Some</span> text </b></div>");
		assertEquals("Some text ", serializer.getXmlAsString(cleaned));
	}
}
