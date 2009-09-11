/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.htmlcleaner;

public class SpecialEntity{
    private final String key;
    private final int intCode;
    // escaped value outputed when generating html 
    private final String htmlString;
    private boolean htmlSpecialEntity;
    // escaped value when outputting html
    private final String escapedXmlString;

    /**
     *
     * @param key value between & and the ';' example 'amp' for '&amp;'
     * @param intCode
     * @param htmlString
     * @param htmlSpecialEntity entity is affected by translateSpecialEntities property setting.
     */
    public SpecialEntity(String key, int intCode, String htmlString, boolean htmlSpecialEntity) {
        this.key = key;
        this.intCode = intCode;
        String str = "&" + key +";";
        if ( htmlString != null) {
            this.htmlString = htmlString;
        } else {
            this.htmlString = str;
        }
        if ( htmlSpecialEntity ) {
            // why not just output the unicode &#(intCode) ???
            this.escapedXmlString = "&amp;"+this.key+";";
        } else {
            this.escapedXmlString = str;
        }
        this.htmlSpecialEntity = htmlSpecialEntity;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the intCode
     */
    public int intValue() {
        return intCode;
    }

    /**
     * @return the domString
     */
    public String getHtmlString() {
        return htmlString;
    }

    public String getEscapedXmlString() {
        return this.escapedXmlString;
    }
    
    public String getEscaped(boolean htmlEscaped) {
        return htmlEscaped?this.getHtmlString():this.getEscapedXmlString();
    }

    /**
     * @return the translateSpecialEntities
     */
    public boolean isHtmlSpecialEntity() {
        return htmlSpecialEntity;
    }

    /**
     * @return {@link #intValue()} cast to an char
     */
    public char charValue() {
        return (char) intValue();
    }
}