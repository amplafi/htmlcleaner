package org.htmlcleaner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.htmlcleaner.Display.block;

import static org.htmlcleaner.Utils.isEmptyString;

/**
 * Checks if node is a <b>block</b> element and has empty contents or white/non-breakable spaces only. Nodes that have
 * non-empty id attribute are considered to be non-empty, since they can be used in javascript scenarios.
 * @author Konsatntin Burov
 */
public class TagNodeEmptyBlockElementCondition implements ITagNodeCondition {

    private static final String ID_ATTRIBUTE_NAME = "id";

    private ITagInfoProvider tagInfoProvider;

    /**
     * Removal of element from this set can affect layout too hard.
     */
    private static final Set < String > unsafeBlockElements = new HashSet < String >();

    static {
        unsafeBlockElements.add("td");
    }

    public TagNodeEmptyBlockElementCondition(ITagInfoProvider provider) {
        this.tagInfoProvider = provider;
    }

    @Override
    public boolean satisfy(TagNode tagNode) {
        String name = tagNode.getName();
        TagInfo tagInfo = tagInfoProvider.getTagInfo(name);
        if (tagInfo == null || hasIdAttributeSet(tagNode) || !block.matchesTagDisplay(tagInfo) || unsafeBlockElements.contains(name)) {
            return false;
        }
        String contentString = tagNode.getText().toString();
        String text = Utils.escapeXml(contentString, true, false, false, false);
        text = text.replace(SpecialEntities.NON_BREAKABLE_SPACE, ' ');
        return isEmptyString(text);
    }

    private boolean hasIdAttributeSet(TagNode tagNode) {
        Map < String, String > attributes = tagNode.getAttributes();
        return !isEmptyString(attributes.get(ID_ATTRIBUTE_NAME));
    }

}