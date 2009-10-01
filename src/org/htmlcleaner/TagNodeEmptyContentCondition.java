package org.htmlcleaner;

import java.util.Set;

/**
 * Checks if node has empty contents or nbsp special entities only.
 * 
 * @author Konsatntin Burov
 *
 */
public class TagNodeEmptyContentCondition implements ITagNodeCondition{

	private static final String SPECIAL_SPACE = "&nbsp;";
	private ITagInfoProvider tagInfoProvider;

	public TagNodeEmptyContentCondition(ITagInfoProvider provider) {
		this.tagInfoProvider = provider;
	}
	
	@Override
	public boolean satisfy(TagNode tagNode) {
		TagInfo tagInfo = tagInfoProvider.getTagInfo(tagNode.getName());
		if(tagInfo.isEmptyTag()){
			return false;
		}
		String text = tagNode.getText().toString();
		if(text.contains(SPECIAL_SPACE)){
			text = text.replace(SPECIAL_SPACE, "");
		}
        return text.trim().isEmpty();
	}

}