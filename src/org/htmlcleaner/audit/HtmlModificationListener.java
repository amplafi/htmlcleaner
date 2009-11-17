package org.htmlcleaner.audit;

import org.htmlcleaner.ITagNodeCondition;
import org.htmlcleaner.TagNode;

/**
 * Implementors can be registered on {@link org.htmlcleaner.CleanerProperties} to receive notifications about
 * modifications made by html cleaner.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public interface HtmlModificationListener {

    /**
     * Fired when cleaner fixes some error in html syntax.
     * 
     * @param certain - true if change made doesn't hurts end document.
     * @param tagNode - problematic node.
     * @param message - human-readable message about the change.
     */
    void fireHtmlError(boolean certainty, TagNode tagNode, ErrorType type);

    /**
     * Fired when cleaner fixes ugly html -- when syntax was correct but task was implemented by weird code.
     * For example when deprecated tags are removed.
     * 
     * @param certainty - true if change made doesn't hurts end document.
     * @param tagNode - problematic node.
     * @param message - human-readable message about the change.
     */
    void fireUglyHtml(boolean certainty, TagNode tagNode, ErrorType errorType);

    /**
     * Fired when cleaner modifies html due to {@link ITagNodeCondition} match.
     * 
     * @param condition that was applied to make the modification
     * @param tagNode - problematic node.
     */
    void fireConditionModification(ITagNodeCondition condition, TagNode tagNode);

    /**
     * Fired when cleaner modifies html due to user specified rules.
     * 
     * @param certainty - true if change made doesn't hurts end document.
     * @param tagNode - problematic node.
     * @param message - human-readable message about the change.
     */
    void fireUserDefinedModification(boolean certainty, TagNode tagNode, ErrorType errorType);

}
