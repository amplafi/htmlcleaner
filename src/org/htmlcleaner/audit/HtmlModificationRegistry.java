package org.htmlcleaner.audit;

import org.htmlcleaner.AttributeTransformation;
import org.htmlcleaner.ITagNodeCondition;
import org.htmlcleaner.TagTransformation;

/**
 * Provide information about correlation between {@link org.htmlcleaner.ITagNodeCondition}s,
 * {@link org.htmlcleaner.TagTransformation}s and {@link HtmlIssue}s.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public interface HtmlModificationRegistry {
    
    /**
     * TODO
     * 
     * @param tagNodeCondition
     * @param issue
     */
    void addModificationCondition(ITagNodeCondition tagNodeCondition, HtmlModification modification);
 
    /**
     * TODO
     * 
     * @param tagNodeCondition
     * @param issue
     */
    void addModificationCondition(TagTransformation tagTransformation, HtmlModification modification);
    
    /**
     * TODO
     * 
     * @param tagNodeCondition
     * @param issue
     */
    void addModificationCondition(AttributeTransformation tagTransformation, HtmlModification modification);
    
    /**
     * TODO
     * 
     * @param tagNodeCondition
     * @return
     */
    HtmlModification getModification(ITagNodeCondition tagNodeCondition);
    
    /**
     * TODO
     * 
     * @param tagNodeCondition
     * @return
     */
    HtmlModification getModification(TagTransformation tagNodeCondition);
  
    /**
     * TODO
     * 
     * @param tagNodeCondition
     * @return
     */
    HtmlModification getModification(AttributeTransformation tagNodeCondition);
    
    
}
