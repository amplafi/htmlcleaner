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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class DocTypesTest {
	
	static HtmlCleaner cleaner;
	static SimpleHtmlSerializer serializer;
	
	@BeforeClass
	public static void setup(){
		cleaner = new HtmlCleaner();
		CleanerProperties properties = cleaner.getProperties();
		properties.setOmitXmlDeclaration(true);
		properties.setOmitDoctypeDeclaration(false);
		serializer = new SimpleHtmlSerializer(properties);
	}

	
	//
	// Check all the valid doctypes
	//
	
	@Test
	public void html_5() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals(null, cleaned.getDocType().getPart2());
		assertEquals("", cleaned.getDocType().getPublicId());
		assertEquals("", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML5, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_5_upper() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals(null, cleaned.getDocType().getPart2());
		assertEquals("", cleaned.getDocType().getPublicId());
		assertEquals("", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML5, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_5_legacy() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML SYSTEM \"about:legacy-compat\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("SYSTEM", cleaned.getDocType().getPart2());
		assertEquals("about:legacy-compat", cleaned.getDocType().getPublicId());
		assertEquals("", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML5_LEGACY_TOOL_COMPATIBLE, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_5_legacy_alternate() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML SYSTEM 'about:legacy-compat'><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("SYSTEM", cleaned.getDocType().getPart2());
		assertEquals("about:legacy-compat", cleaned.getDocType().getPublicId());
		assertEquals("", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML5_LEGACY_TOOL_COMPATIBLE, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}

	@Test
	public void html_4_0() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.0//EN", cleaned.getDocType().getPublicId());
		assertEquals("", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_0, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_0_strict() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.0//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/REC-html40/strict.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_0, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_strict_identifierOnly() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.01//EN", cleaned.getDocType().getPublicId());
		assertEquals("", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_01_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_strict_mixed() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.01//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/html4/strict.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_01_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_strict() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.01//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/html4/strict.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_01_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_transitional() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.01 Transitional//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/html4/loose.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_01_TRANSITIONAL, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_frameset() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\"><html><body></body></html>");
		assertEquals("HTML", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD HTML 4.01 Frameset//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/html4/frameset.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.HTML4_01_FRAMESET, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_strict() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.XHTML1_0_STRICT, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_transitional() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.XHTML1_0_TRANSITIONAL, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_frameset() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\"><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD XHTML 1.0 Frameset//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.XHTML1_0_FRAMESET, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_1() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD XHTML 1.1//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.XHTML1_1, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_1_basic() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd\"><html><body></body></html>");
		assertEquals("html", cleaned.getDocType().getPart1());
		assertEquals("PUBLIC", cleaned.getDocType().getPart2());
		assertEquals("-//W3C//DTD XHTML Basic 1.1//EN", cleaned.getDocType().getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd", cleaned.getDocType().getSystemId());
		assertEquals(DoctypeToken.XHTML1_1_BASIC, cleaned.getDocType().getType());
		assertTrue(cleaned.getDocType().isValid());
	}
	
	//
	// Now some invalid ones
	//

	@Test
	public void empty() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE><html><body></body></html>");
		assertEquals(DoctypeToken.UNKNOWN, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void not_html() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE banana><html><body></body></html>");
		assertEquals(DoctypeToken.UNKNOWN, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}

	@Test
	public void html_4_0_wrong_id_type() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML SYSTEM \"-//W3C//DTD HTML 4.0//EN\"><html><body></body></html>");
		assertEquals(DoctypeToken.UNKNOWN, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_0_wrong_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd\"><html><body></body></html>");
		assertEquals(DoctypeToken.HTML4_0, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_wrong_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd\"><html><body></body></html>");
		assertEquals(DoctypeToken.HTML4_01_STRICT, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_transitional_bad_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd\"><html><body></body></html>");
		assertEquals(DoctypeToken.HTML4_01_TRANSITIONAL, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void html_4_01_frameset_bad_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"><html><body></body></html>");
		assertEquals(DoctypeToken.HTML4_01_FRAMESET, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}

	@Test
	public void xhtml_1_0_with_wrong_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd\"><html><body></body></html>");
		assertEquals(DoctypeToken.XHTML1_0_STRICT, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_0_transitional_with_wrong_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"><html><body></body></html>");
		assertEquals(DoctypeToken.XHTML1_0_TRANSITIONAL, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_0_frameset_with_wrong_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\"><html><body></body></html>");
		assertEquals(DoctypeToken.XHTML1_0_FRAMESET, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_1_with_wrong_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd\"><html><body></body></html>");
		assertEquals(DoctypeToken.XHTML1_1, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void xhtml_1_1_with_no_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"><html><body></body></html>");
		assertFalse(cleaned.getDocType().isValid());
		assertEquals(DoctypeToken.XHTML1_1, cleaned.getDocType().getType());
	}
	
	@Test
	public void xhtml_1_1_basic_with_no_id() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\"><html><body></body></html>");
		assertEquals(DoctypeToken.XHTML1_1_BASIC, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	@Test
	public void weird_token() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html SILLY \"-//W3C//DTD XHTML Basic 1.1//EN\"><html><body></body></html>");
		assertEquals(DoctypeToken.UNKNOWN, cleaned.getDocType().getType());
		assertFalse(cleaned.getDocType().isValid());
	}
	
	//
	// Serializer
	//

	@Test
	public void html_4_01_serialize() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"));
	}
	
	@Test
	public void html_4_01_case_correct() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"));
	}
	
	@Test
	public void xhtml_1_1_serialize() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\"><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\">"));
	}
	
	@Test
	public void xhtml_1_0_strict_serialize() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"));
	}
	
	@Test
	public void xhtml_1_0_strict_serialize_case_correct() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"));
	}
	
	@Test
	public void html5_serialize() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE html><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE html>"));
	}
	
	@Test
	public void html5_serialize_case_correct() throws IOException{
		TagNode cleaned = cleaner.clean("<!DOCTYPE HTML><html><body></body></html>");
		String output = serializer.getAsString(cleaned);
		assertTrue(output.startsWith("<!DOCTYPE html>"));
	}
	
	
	//
	// Misc
	//
	
	@Test
	public void checkToString(){
		TagNode cleaned = cleaner.clean("<!DOCTYPE html><html><body></body></html>");
		assertEquals(cleaned.getDocType().getContent(), cleaned.getDocType().toString());
	}
}
