package org.htmlcleaner;

/**
 * Checks if node has empty contents.
 * 
 * @author Konsatntin Burov
 *
 */
public class TagNodeEmptyContentCondition implements ITagNodeCondition{

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
        return text.trim().isEmpty();
	}
	
}