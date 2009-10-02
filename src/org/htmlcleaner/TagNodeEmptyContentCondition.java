package org.htmlcleaner;


/**
 * Checks if node has empty contents or white/non-breakable spaces only.
 * 
 * @author Konsatntin Burov
 *
 */
public class TagNodeEmptyContentCondition implements ITagNodeCondition{

	private static final char WHITESPACE = 20;

	private static final char NON_BREAKABLE_SPACE = 160;
	
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
		String text = Utils.escapeXml(tagNode.getText().toString(), true, false, false, false);
		text = text.replace(NON_BREAKABLE_SPACE, WHITESPACE);
        return text.trim().isEmpty();
	}

}