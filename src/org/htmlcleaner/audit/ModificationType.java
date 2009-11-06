package org.htmlcleaner.audit;

/**
 * 
 * Defines type of possible problems/modifications that cleaner had to fix/make.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public enum ModificationType {

    /**
     * Syntax problem or any other thing that doesn't matches w3c recommendations. 
     */
    BAD_HTML, 
    
    /**
     * Not an error but rather a bad style of implementing something.
     */
    YECKY_HTML, 
    
    /**
     * Optimizations to make code more friendly for search engines.
     */
    SEO_HTML, 
    
    /**
     * Any other changes that user requested.
     */
    USER_DEFINED 
    
}
