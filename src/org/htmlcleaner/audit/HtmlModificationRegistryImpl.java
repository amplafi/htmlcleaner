package org.htmlcleaner.audit;

import java.util.HashMap;
import java.util.Map;

import org.htmlcleaner.AttributeTransformation;
import org.htmlcleaner.ITagNodeCondition;
import org.htmlcleaner.TagTransformation;

/**
 * TODO
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class HtmlModificationRegistryImpl implements HtmlModificationRegistry {

    private Map < ITagNodeCondition, HtmlModification > tagNodeConditionModifications = new HashMap < ITagNodeCondition, HtmlModification >();

    @Override
    public void addModificationCondition(ITagNodeCondition tagNodeCondition, HtmlModification modification) {
        tagNodeConditionModifications.put(tagNodeCondition, modification);
    }

    @Override
    public void addModificationCondition(TagTransformation tagTransformation, HtmlModification modification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addModificationCondition(AttributeTransformation tagTransformation, HtmlModification modification) {
        // TODO Auto-generated method stub

    }

    @Override
    public HtmlModification getModification(ITagNodeCondition tagNodeCondition) {
        HtmlModification htmlModification = tagNodeConditionModifications.get(tagNodeCondition);
        if (htmlModification != null) {
            htmlModification = htmlModification.clone();
        }
        return htmlModification;
    }

    @Override
    public HtmlModification getModification(TagTransformation tagNodeCondition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HtmlModification getModification(AttributeTransformation tagNodeCondition) {
        // TODO Auto-generated method stub
        return null;
    }

}
