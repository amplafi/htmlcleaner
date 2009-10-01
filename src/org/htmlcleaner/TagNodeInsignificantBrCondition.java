package org.htmlcleaner;

import java.util.List;
import java.util.Set;

/**
 * Checks if node is an insignificant br tag -- is placed at the end or at the
 * start of a block.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class TagNodeInsignificantBrCondition implements ITagNodeCondition {

	private static final String BR_TAG = "br";
	
	private Set<ITagNodeCondition> conditions;

	public TagNodeInsignificantBrCondition(Set<ITagNodeCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean satisfy(TagNode tagNode) {
		if (tagNode == null || !BR_TAG.equals(tagNode.getName())) {
			return false;
		}
		TagNode parent = tagNode.getParent();
		List children = parent.getChildren();
		int brIndex = children.indexOf(tagNode);
		if(brIndex == 0 || brIndex == children.size() - 1){
			return true;
		} else{
			return checkSublist(0, brIndex, children) || checkSublist (brIndex, children.size(), children);
		}
	}

	private boolean checkSublist(int start, int end, List list) {
		List sublist = list.subList(start, end);
		for (Object object : sublist) {
			if(!(object instanceof TagNode)){
				return false;
			}
			TagNode node = (TagNode) object;
			if(!BR_TAG.equals(node.getName())&&!isPruned(node)){
				return false;
			}
		}
		return true;
	}

	private boolean isPruned(TagNode node) {
		for (ITagNodeCondition condition : conditions) {
			if(condition.satisfy(node)){
				return true;
			}
		}
		return false;
	}

}
