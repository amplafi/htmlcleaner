package org.htmlcleaner;

import java.util.Map;
import static org.htmlcleaner.Utils.isEmptyString;
import static org.htmlcleaner.Display.inline;

/**
 * Checks if node is an <b>inline</b> element and has empty contents or white/non-breakable spaces only. Nodes that have
 * non-empty id attribute are considered to be non-empty, since they can be used in javascript scenarios.
 * @author Konsatntin Burov
 */
public class TagNodeEmptyContentCondition implements ITagNodeCondition {

    private static final String ID_ATTRIBUTE_NAME = "id";

    private ITagInfoProvider tagInfoProvider;

    public TagNodeEmptyContentCondition(ITagInfoProvider provider) {
        this.tagInfoProvider = provider;
    }

    @Override
    public boolean satisfy(TagNode tagNode) {
        TagInfo tagInfo = tagInfoProvider.getTagInfo(tagNode.getName());
      //Only _inline_ elements can match.
        if (tagInfo != null && !tagInfo.isEmptyTag() && !hasIdAttributeSet(tagNode) && inline == tagInfo.getDisplay()) {
            String contentString = tagNode.getText().toString();
            String text = Utils.escapeXml(contentString, true, false, false, false);
            text = text.replace(SpecialEntities.NON_BREAKABLE_SPACE, ' ');
            return isEmptyString(text);
        }
        return false;
    }

    private boolean hasIdAttributeSet(TagNode tagNode) {
        Map < String, String > attributes = tagNode.getAttributes();
        return !isEmptyString(attributes.get(ID_ATTRIBUTE_NAME));
    }

}