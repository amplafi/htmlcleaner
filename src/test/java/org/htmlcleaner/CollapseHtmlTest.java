package org.htmlcleaner;

import junit.framework.TestCase;

/**
 *  Various tests for collapseNullHtml mode.
 */
public class CollapseHtmlTest extends TestCase {

    /**
     * 
     */
    private static final String CANNOT_ELIMINATE_ANYTHING_IN_THIS_TR = "<tr><td></td><td>Cannot eliminate anything in this row</td></tr>";

    /**
     * 
     */
    private static final String IMAGE = "<img src=\"http://localhost:8080/img/foo.jpg\" />";

    /**
     * 
     */
    private static final String DONT_COLLAPSE =
        "<span>" + IMAGE  +"</span>" +
        "<p>" + IMAGE +"</p>" +
    		"<p>bar<table><tr><td></td><td>"+IMAGE+"</td><td> </td></tr></table>foo</p>";
    private static final String DONT_COLLAPSE_OUTPUT = 
    "<span>" + IMAGE +"</span>" +
    "<p>" +IMAGE +"</p>" +
    "<p>bar</p><table><tbody><tr><td></td><td>"+IMAGE+"</td><td> </td></tr></tbody></table><p>foo</p>";
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
        properties.addPruneTagNodeCondition(new TagNodeEmptyContentCondition(properties.getTagInfoProvider()));
        properties.addPruneTagNodeCondition(new TagNodeInsignificantBrCondition());
    }

    /**
     * Make sure that single empty tag is dropped out.
     */
    public void testCollapseSingleEmptyTag() {
        TagNode collapsed = cleaner.clean("<u></u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
    }

    /**
     * Make sure that tags with internal blanks are collapsed.
     */
    public void testCollapseSingleTagWithBlanks() {
        TagNode collapsed = cleaner.clean("<u>   </u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<u> &#x20;  </u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        //Strange msword insert
        //collapsed = cleaner.clean("<span style='mso-spacerun:yes'>  </span>");
        //assertEquals("", serializer.getXmlAsString(collapsed));
    }

    /**
     * make sure that non-breaking spaces are also collapsed away.
     */
    public void testCollapseSingleTagWithNbsp() {
        TagNode collapsed = cleaner.clean("<u> &nbsp; </u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<u> &#160; </u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<u> &#xA0; </u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<u> "+SpecialEntities.NON_BREAKABLE_SPACE+" </u>");
        assertEquals("", serializer.getXmlAsString(collapsed));
    }

    /**
     * make sure that multiple null tags are collapsed.
     */
    public void testCollapseMultipleEmptyTags() {
        TagNode collapsed = cleaner.clean("<b><i><u></u></i></b>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        
        // test with slightly bad html.
        collapsed = cleaner.clean("<b><i><u></i></u></b>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        // test with slightly bad html.
        collapsed = cleaner.clean("<b><i><u></i></u>notme</b>");
        assertEquals("<b>notme</b>", serializer.getXmlAsString(collapsed));
    }

    /**
     *  make sure that insignificant br tags are collapsed
     */
    public void testCollapseInsignificantBr() {
        TagNode collapsed = cleaner.clean("<p><br/>Some text</p>");
        assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<p>Some text<BR/></p>");
        assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<p><br/>Some<br/> text<br/></p>");
        assertEquals("<p>Some<br /> text</p>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<p><br/><br/>Some text <i>look here</i></p>");
        assertEquals("<p>Some text <i>look here</i></p>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("Some text<BR/>");
        assertEquals("Some text", serializer.getXmlAsString(collapsed));
    }
    
    /**
     * make sure TagTransformations do not interfere with collapse
     */
    public void testCollapseEmptyWithTagTransformations() {
        CleanerTransformations transformations = properties.getCleanerTransformations();
        TagTransformation t = new TagTransformation("font", "span", true);
        t.addAttributeTransformation("style", "${style};font-family:${face};font-size:${size};color:${color};");
        t.addAttributeTransformation("face");
        t.addAttributeTransformation("size");
        t.addAttributeTransformation("color");
        t.addAttributeTransformation("name", "${face}_1");
        transformations.addTransformation(t);
        TagNode collapsed = cleaner.clean("<b><font face=\"Ariel\"><u></u></font></b>");
        assertEquals("", serializer.getXmlAsString(collapsed));
    }

    /**
     * test to make sure that multiple <br> elements are eliminated
     */
    public void testChainCollapseInsignificantBrs() {
        TagNode collapsed = cleaner.clean("<p><br/><br>Some<br>text<br/><br><br></p>");
        assertEquals("<p>Some<br />text</p>", serializer.getXmlAsString(collapsed));
    }

    /**
     * make sure that intervening empty elements still cause unneeded <br> s to be eliminated.
     */
    public void testCollapseInsignificantBrWithEmptyElements() {
        TagNode collapsed = cleaner.clean("<p><span>&nbsp;</span><br/>Some text</p>");
        assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<p>Some text<br><span></span><BR/><u><big></big></u><BR/></p>");
        assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<p>Some text<br><span></span><BR/><u><big></big></u><BR/><u></u></p>");
        assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
        
    }
    /**
     * Br nested in formating elements should be eliminated.
     */
    public void testInsureMeaninglessBrsStillCollapseEmptyElements() {
        TagNode collapsed = cleaner.clean("<p><u><br/></u>Some text<br><span><BR/><u><big><BR/></big></u></p></span>");
        assertEquals("<p>Some text</p>", serializer.getXmlAsString(collapsed));
    }
    
    /**
     * because elements with ids can be referred to by javascript, don't assume that such elements can be eliminated.
     */
    public void testCollapseOnlyFormattingElementsWithNoIds() {
        TagNode collapsed = cleaner.clean("<b id=\"notme\"></b><span></span><span id=\"norme\"></span>");
        assertEquals("<b id=\"notme\"></b><span id=\"norme\"></span>", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<b iD=\"notme\"></b><span></span><span ID=\"norme\"></span>");
        assertEquals("<b id=\"notme\"></b><span id=\"norme\"></span>", serializer.getXmlAsString(collapsed));
    }

    public void testCollapseAggressively() {
        properties.addPruneTagNodeCondition(new TagNodeEmptyContentCondition(properties.getTagInfoProvider()));
        TagNode collapsed;
        collapsed = cleaner.clean("<p><table><tr></tr><tr><td></td></tr></table></p>");
        assertEquals("", serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean(DONT_COLLAPSE);
        assertEquals(DONT_COLLAPSE_OUTPUT, serializer.getXmlAsString(collapsed));
        collapsed = cleaner.clean("<p id=\"notme\"></p><table><tr></tr><tr><td>Nor me</td></tr><tr><td></td></tr><tr> </tr>" +
        		"<tr>&nbsp;\n</tr>" +
        		CANNOT_ELIMINATE_ANYTHING_IN_THIS_TR +
        		"</table>");
        assertEquals("<p id=\"notme\"></p><table><tbody><tr><td>Nor me</td></tr>" +
            CANNOT_ELIMINATE_ANYTHING_IN_THIS_TR +
        		"</tbody></table>", serializer
                .getXmlAsString(collapsed));
    }
}
