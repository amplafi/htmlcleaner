package org.htmlcleaner;

/**
 * Checks if node is an insignificant br tag -- is placed at the end or at the
 * start of a block.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class TagNodeInsignificantBrCondition implements ITagNodeCondition {

	@Override
	public boolean satisfy(TagNode tagNode) {
		if (tagNode == null || !"br".equals(tagNode.getName())) {
			return false;
		}
		TagNode parent = tagNode.getParent();
		TagNode[] allElements = parent.getAllElements(false);
		if (allElements[0].equals(tagNode)
				|| allElements[allElements.length - 1].equals(tagNode)) {
			return true;
		}
		return false;
	}

}
