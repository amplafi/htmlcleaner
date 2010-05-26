package org.htmlcleaner;

/**
 * A {@link TagNode} that only really holds whitespace - allows
 * using {@link ContentToken} in places where a {@link TagNode} is expected.
 * <p/>
 * This class is currently just a short-lived intermediate artifact generated 
 * from {@link HtmlCleaner} while cleaning an html file and descarded 
 * before the results are returned.
 * 
 * @author andyhot
 */
class WhitespaceTagNode extends TagNode {
	private ContentToken token;
	private TagNode bodyNode;
	
	public WhitespaceTagNode(ContentToken token, TagNode bodyNode) {
		super("");
		this.token = token;
		this.bodyNode = bodyNode;
	}

	@Override
	public TagNode getParent() {
		return null;
	}
	
	@Override
	public boolean removeFromTree() {
		bodyNode.removeChild(token);
		return true;
	}	
	
	public ContentToken getContentToken() {
		return token;
	}	
	
	public String getContent() {
		return token.getContent();
	}

}
