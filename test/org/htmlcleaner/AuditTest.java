package org.htmlcleaner;

import java.util.List;

import org.htmlcleaner.TagNode.TagNodeNameCondition;
import org.htmlcleaner.audit.Certainty;
import org.htmlcleaner.audit.HtmlModification;
import org.htmlcleaner.audit.HtmlModificationManager;
import org.htmlcleaner.audit.ModificationType;

import junit.framework.TestCase;

/**
 * 
 * Tests that html cleaner correctly creates audit entries for different html modifications.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public class AuditTest extends TestCase{

    
    public void testUserDefinedTagsRemovalAudit(){
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setOmitXmlDeclaration(true);
        properties.setPruneTags("b");
        cleaner.clean("<html><head/><body><b>some</b></body></html>");
        assertEquals(1, cleaner.getHtmlModificationManager().getModificationCount(null, null));
        //TODO add more testcases
    }
    
    public void testTagNodeConditionAudit(){
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setOmitXmlDeclaration(true);
        String message = "B tag is rather deprecated, removed.";
        properties.addPruneTagNodeCondition(new TagNodeNameCondition("b"), new HtmlModification(ModificationType.YECKY_HTML, Certainty.CERTAIN, null, message));
        cleaner.clean("<html><head/><body><b>some</b></body></html>");
        HtmlModificationManager htmlModificationManager = cleaner.getHtmlModificationManager();
        assertEquals(1, htmlModificationManager.getModificationCount(null, null));
        assertEquals(message, htmlModificationManager.getModifications(null, null).get(0).getMessage());
    }
    
    public void testUnclosedTagAudit(){
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();
        properties.setOmitXmlDeclaration(true);
        cleaner.clean("<html><head/><body><b>some</body></html>");
        int issueCount = cleaner.getHtmlModificationManager().getModificationCount(null, null);
        List < HtmlModification > modifications = cleaner.getHtmlModificationManager().getModifications(null, null);
        assertEquals(1, issueCount);
        assertEquals(ModificationType.BAD_HTML, modifications.get(0).getType());
    }
}
