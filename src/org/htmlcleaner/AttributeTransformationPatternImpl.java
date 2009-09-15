/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential. 
 */
package org.htmlcleaner;

import java.util.regex.Pattern;

public class AttributeTransformationPatternImpl implements AttributeTransformation {
    private final Pattern attNamePattern;
    private final Pattern attValuePattern;
    private final String template;
    public AttributeTransformationPatternImpl(Pattern attNamePattern, Pattern attValuePattern, String template) {
        this.attNamePattern = attNamePattern;
        this.attValuePattern = attValuePattern;
        this.template = template;
    }
    public AttributeTransformationPatternImpl(String attNamePattern, String attValuePattern, String template) {
        this.attNamePattern = attNamePattern ==null?null:Pattern.compile(attNamePattern);
        this.attValuePattern = attValuePattern == null? null: Pattern.compile(attValuePattern);
        this.template = template;
    }

    public boolean satisfy(String attName, String attValue) {
        if ( (attNamePattern == null || attNamePattern.matcher(attName).find()) && (attValuePattern ==null || attValuePattern.matcher(attValue).find())){
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }
}