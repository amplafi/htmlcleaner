package org.htmlcleaner;

import java.io.IOException;

import junit.framework.TestCase;

/**
 *  Simple test to check that randomly appeared end tags are dropped out.
 */
public class RandomCloseTagTest extends TestCase {
	
	public void testRandomCloseTagsRemoved() throws IOException{
		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties properties = cleaner.getProperties();
		properties.setOmitHtmlEnvelope(true);
		properties.setOmitXmlDeclaration(true);
		SimpleXmlSerializer serializer = new SimpleXmlSerializer(properties);
		TagNode cleaned = cleaner.clean("Some</span> text </b></div>");
		assertEquals("Some text ", serializer.getAsString(cleaned));
	}
}
