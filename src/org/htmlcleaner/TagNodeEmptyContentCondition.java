package org.htmlcleaner;

import java.util.Map;
import static org.htmlcleaner.Utils.isEmptyString;

/**
 * Checks if node has empty contents or white/non-breakable spaces only. Nodes
 * that have non-empty id attribute are considered to be non-empty, since they
 * can be used in javascript scenarios.
 * 
 * @author Konsatntin Burov
 * 
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
		if (tagInfo.isEmptyTag() || hasIdAttributeSet(tagNode)
				|| Display.inline != tagInfo.getDisplay()) {
			return false;
		}
		String contentString = tagNode.getText().toString();
        String text = Utils.escapeXml(contentString, true, false, false, false);
		text = text.replace(SpecialEntities.NON_BREAKABLE_SPACE, ' ');
		return isEmptyString(text);
	}

	private boolean hasIdAttributeSet(TagNode tagNode) {
		Map<String, String> attributes = tagNode.getAttributes();
		return !isEmptyString(attributes.get(ID_ATTRIBUTE_NAME));
	}

}