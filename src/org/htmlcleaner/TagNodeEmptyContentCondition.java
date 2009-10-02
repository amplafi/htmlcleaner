package org.htmlcleaner;

import java.util.Map;
import static org.htmlcleaner.Utils.isEmptyString;


/**
 * Checks if node has empty contents or white/non-breakable spaces only.
 * Nodes that have non-empty id attribute are considered to be non-empty, since 
 * they can be used in javascript scenarios.
 * 
 * @author Konsatntin Burov
 *
 */
public class TagNodeEmptyContentCondition implements ITagNodeCondition{

	private static final String ID_ATTRIBUTE_NAME = "id";

	private static final char WHITESPACE = 20;

	private static final char NON_BREAKABLE_SPACE = 160;
	
	private ITagInfoProvider tagInfoProvider;

	public TagNodeEmptyContentCondition(ITagInfoProvider provider) {
		this.tagInfoProvider = provider;
	}
	
	@Override
	public boolean satisfy(TagNode tagNode) {
		TagInfo tagInfo = tagInfoProvider.getTagInfo(tagNode.getName());
		if(tagInfo.isEmptyTag() || hasIdAttributeSet(tagNode)){
			return false;
		}
		String text = Utils.escapeXml(tagNode.getText().toString(), true, false, false, false);
		text = text.replace(NON_BREAKABLE_SPACE, WHITESPACE);
        return isEmptyString(text);
	}

	private boolean hasIdAttributeSet(TagNode tagNode) {
		Map<String, String> attributes = tagNode.getAttributes();
		return !isEmptyString(attributes.get(ID_ATTRIBUTE_NAME));
	}

}