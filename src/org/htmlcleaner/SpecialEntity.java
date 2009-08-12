/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.htmlcleaner;

public class SpecialEntity{
    private final String key;
    private final int intCode;
    private final String domString;
    private boolean htmlSpecialEntity;
    private final String xmlString;

    /**
     *
     * @param key value between & and the ';' example 'amp' for '&amp;'
     * @param intCode
     * @param domString
     * @param htmlSpecialEntity entity is affected by translateSpecialEntities property setting.
     */
    public SpecialEntity(String key, int intCode, String domString, boolean htmlSpecialEntity) {
        this.key = key;
        this.intCode = intCode;
        String xmlString = "&" + key +";";
        if ( domString != null) {
            this.domString = domString;
        } else {
            this.domString = xmlString;
        }
        if ( htmlSpecialEntity ) {
            this.xmlString = "&amp;"+this.key+";";
        } else {
            this.xmlString = xmlString;
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
    public String getDomString() {
        return domString;
    }

    public String getXmlString() {
        return this.xmlString;
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