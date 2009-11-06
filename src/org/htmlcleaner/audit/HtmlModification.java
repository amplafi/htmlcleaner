/**
 * 
 */
package org.htmlcleaner.audit;

import org.htmlcleaner.TagNode;

/**
 * Represents any html problem/optimization/change that cleaner
 * makes during cleanup process. 
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public class HtmlModification implements Cloneable{

    /**
     * Type of modification that was made.
     */
    private ModificationType type;
    
    /**
     * Certainty level.
     */
    private Certainty certainty;
    
    /**
     * Xpath to the node that was source of the issue.
     */
    private String xpath;
    
    /**
     * Human readable message describing the problem.
     */
    private String message;
    
    /**
     * Problematic node.
     */
    private transient TagNode tagNode;

    public HtmlModification(ModificationType type, Certainty certainty, String xpath, String message) {
        super();
        this.type = type;
        this.certainty = certainty;
        this.xpath = xpath;
        this.message = message;
    }

    public ModificationType getType() {
        return type;
    }

    public void setType(ModificationType type) {
        this.type = type;
    }

    public Certainty getCertainty() {
        return certainty;
    }

    public void setCertainty(Certainty certainty) {
        this.certainty = certainty;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TagNode getTagNode() {
        return tagNode;
    }

    public void setTagNode(TagNode tagNode) {
        this.tagNode = tagNode;
    }
    
    public HtmlModification clone(){
        return new HtmlModification(type, certainty, xpath, message);
    }
}
