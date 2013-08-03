/*  Copyright (c) 2006-2013, HtmlCleaner project team (Vladimir Nikic, Scott Wilson, Pat Moore)
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of HtmlCleaner may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "HtmlCleaner" in the
    subject line.
*/

package org.htmlcleaner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class XmlDeclarationsTest {
	
	static HtmlCleaner cleaner;
	static CompactXmlSerializer serializer;
	
	static final String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html>\n<head />\n<body><p>test</p></body></html>";
	
	@BeforeClass
	public static void setup(){
		cleaner = new HtmlCleaner();
		CleanerProperties properties = cleaner.getProperties();
		properties.setOmitXmlDeclaration(false);
		properties.setOmitDoctypeDeclaration(false);
		properties.setIgnoreQuestAndExclam(false);
		serializer = new CompactXmlSerializer(properties);
	}

	//
	// No Newlines
	//
	@Test
	public void checkXml(){
		TagNode cleaned = cleaner.clean("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html><head /><body><p>test</p></body></html>");
		
		String output = serializer.getAsString(cleaned);
		
		assertEquals(DoctypeToken.XHTML1_0_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
		assertEquals(expectedOutput, output);
	}
	
	//
	// Newlines
	//
	@Test
	public void checkWhitespace(){
		TagNode cleaned = cleaner.clean("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html><head /><body><p>test</p></body></html>");

		
		String output = serializer.getAsString(cleaned);
		
		assertEquals(DoctypeToken.XHTML1_0_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
		assertEquals(expectedOutput, output);
	}
	
	/**
	 * This is to test issue #67
	 */
	@Test
	public void checkXmlNoExtraWhitesapce(){
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html><head /><body><p>test</p></body></html>";
		TagNode cleaned = cleaner.clean(expected);
		cleaner.getProperties().setAddNewlineToHeadAndBody(false);
		Serializer theSerializer = new SimpleXmlSerializer(cleaner.getProperties());
		String output = theSerializer.getAsString(cleaned);
		cleaner.getProperties().setAddNewlineToHeadAndBody(true);
		assertEquals(expected, output);
	}

	@Test
	public void checkXmlNoEncoding(){
		TagNode cleaned = cleaner.clean("<?xml version=\"1.0\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html><head /><body><p>test</p></body></html>");
		
		String output = serializer.getAsString(cleaned);
		
		assertEquals(DoctypeToken.XHTML1_0_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
		assertEquals(expectedOutput, output);
	}
	

}
