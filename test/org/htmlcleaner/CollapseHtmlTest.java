package org.htmlcleaner;

import junit.framework.TestCase;

public class CollapseHtmlTest extends TestCase {
	
	private HtmlCleaner cleaner;
	private CleanerProperties properties;
	private SimpleXmlSerializer serializer;

	@Override
	protected void setUp() throws Exception {
		cleaner = new HtmlCleaner();
		properties = cleaner.getProperties();
		properties.setOmitHtmlEnvelope(true);
		properties.setOmitXmlDeclaration(true);
		serializer = new SimpleXmlSerializer(properties);
	}
	
	public void testNoneCollapseMode(){
		properties.setCollapseNullHtml(CollapseHtml.none);
		TagNode collapsed = cleaner.clean("<u></u>");
		assertEquals("<u></u>", serializer.getXmlAsString(collapsed));
	}

	public void testCollapseSingleEmptyTag(){
		properties.setCollapseNullHtml(CollapseHtml.emptyOrBlanks);
		TagNode collapsed = cleaner.clean("<u></u>");
		assertEquals("", serializer.getXmlAsString(collapsed));
	}
	
	public void testCollapseSingleTagWithBlanks(){
		properties.setCollapseNullHtml(CollapseHtml.emptyOrBlanks);
		TagNode collapsed = cleaner.clean("<u>   </u>");
		assertEquals("", serializer.getXmlAsString(collapsed));
	}
	
	public void testCollapseMultipleEmptyTags(){
		properties.setCollapseNullHtml(CollapseHtml.emptyOrBlanks);
		TagNode collapsed = cleaner.clean("<b><i><u></u></i></b>");
		assertEquals("", serializer.getXmlAsString(collapsed));
	}
	
	public void testCollapseInsignificantBr(){
		properties.setCollapseNullHtml(CollapseHtml.emptyOrBlanks);
		TagNode collapsed = cleaner.clean("<p><br/>Some text</p>");
		assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
		collapsed = cleaner.clean("<p>Some text<BR/></p>");
		assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
		collapsed = cleaner.clean("<p><br/>Some<br/> text<br/></p>");
		assertEquals("<p>Some<br /> text</p>", serializer.getXmlAsString(collapsed));
		collapsed = cleaner.clean("<p><br/><br/>Some text</p>");
		assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
	}
}
