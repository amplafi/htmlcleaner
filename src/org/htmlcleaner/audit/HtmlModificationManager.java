package org.htmlcleaner.audit;

import java.util.List;

/**
 * TODO
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public interface HtmlModificationManager {
    
    public void add(HtmlModification modification);
    
    public int getModificationCount(ModificationType type, Certainty certainty);
    
    public List < HtmlModification > getModifications(ModificationType type, Certainty certainty);
    
}
