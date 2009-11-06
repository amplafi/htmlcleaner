package org.htmlcleaner.audit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public class HtmlModificationManagerImpl implements HtmlModificationManager {

    private List < HtmlModification > modifications = new ArrayList < HtmlModification >();
    
    @Override
    public void add(HtmlModification modification) {
        modifications.add(modification);
    }

    @Override
    public int getModificationCount(ModificationType type, Certainty certainty) {
        return getModifications(type, certainty).size();
    }

    @Override
    public List < HtmlModification > getModifications(ModificationType type, Certainty certainty) {
        List < HtmlModification > result = new ArrayList < HtmlModification >(modifications);
        if(type != null){
            for (Iterator<HtmlModification> iterator = result.iterator(); iterator.hasNext();) {
                HtmlModification htmlModification = iterator.next();
                if(!type.equals(htmlModification.getType())){
                    iterator.remove();
                }
            }
        }
        if(certainty != null){
            for (Iterator<HtmlModification> iterator = result.iterator(); iterator.hasNext();) {
                HtmlModification htmlModification = iterator.next();
                if(!certainty.equals(htmlModification.getCertainty())){
                    iterator.remove();
                }
            }
        }
        return result;
    }

}
