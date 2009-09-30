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
     *  KOSTYA - this list should be defined by {@link ITagInfoProvider} in some manner
     */
    emptyOrBlanks;
}
