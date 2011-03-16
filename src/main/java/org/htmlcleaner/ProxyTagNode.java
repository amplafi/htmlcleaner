package org.htmlcleaner;

/**
 * A {@link TagNode} that only really holds whitespace or comments - allows
 * using {@link ContentToken} in places where a {@link TagNode} is expected.
 * <p/>
 * This class is currently just a short-lived intermediate artifact generated 
 * from {@link HtmlCleaner} while cleaning an html file and descarded 
 * before the results are returned.
 * 
 * @author andyhot
 */
class ProxyTagNode extends TagNode {
	private ContentToken token;
	private CommentToken comment;
	private TagNode bodyNode;
	
	public ProxyTagNode(ContentToken token, TagNode bodyNode) {
		super("");
		this.token = token;
		this.bodyNode = bodyNode;
	}
	
	public ProxyTagNode(CommentToken comment, TagNode bodyNode) {
		super("");
		this.comment = comment;
		this.bodyNode = bodyNode;
	}	

	@Override
	public TagNode getParent() {
		return null;
	}
	
	@Override
	public boolean removeFromTree() {
		bodyNode.removeChild(getToken());
		return true;
	}	
	
	public Object getToken() {
		return token!=null ? token : comment;
	}	
	
	public String getContent() {
		return token!=null ? token.getContent() : comment.getContent();
	}

}
