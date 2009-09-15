/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential. 
 */
package org.htmlcleaner;

public interface AttributeTransformation {
    boolean satisfy(String attName, String attValue);
    String getTemplate();
}