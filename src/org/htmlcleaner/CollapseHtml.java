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
     * remove text formatting elements before 
     */
    emptyOrBlanks;
}
