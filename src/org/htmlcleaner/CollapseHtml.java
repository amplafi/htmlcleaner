/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential. 
 */
package org.htmlcleaner;

/**
 * @author patmoore
 *
 */
public enum CollapseHtml {
    /**
     * do not collapse any empty html elements.
     */
    none,
    /**
     * remove text formatting elements with no non-blank content.
     * text formatting elements are:
     * 
     *  KOSTYA - 'inline' elements are indicated by {@link TagInfo} in some manner ( may need to be added -- but check to see if there isn't already such a flag on TagInfo ) 
     *  
     */
    emptyOrBlankInlineElements;
}
