/**
 * 
 */
package org.htmlcleaner.audit;


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
     * TagName of the node that was source of the issue.
     */
    private String tagName;
    
    /**
     * Human readable message describing the problem.
     */
    private String message;
    
    public HtmlModification(ModificationType type, Certainty certainty, String tagName, String message) {
        super();
        this.type = type;
        this.certainty = certainty;
        this.tagName = tagName;
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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public HtmlModification clone(){
        return new HtmlModification(type, certainty, tagName, message);
    }
}
