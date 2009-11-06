package org.htmlcleaner.audit;

/**
 * 
 * Defines possible certainty levels of changes made.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public enum Certainty {

    /**
     * Changes made shoudn't break anything.
     */
    CERTAIN,
    
    /**
     * Changes made were needed, but we're uncertain if it breaks something.
     */
    UNCERTAIN
    
}
