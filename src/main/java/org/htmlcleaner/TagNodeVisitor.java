package org.htmlcleaner;

/**
 * Defines action to be performed on TagNodes
 */
public interface TagNodeVisitor {

    /**
     * Action to be performed on single TagNode in the tree
     * @param parentNode Parent of tagNode
     * @param tagNode tagNode visited
     * @return True if tree traversal should be continued, false if it has to stop.
     */
    public boolean visit(TagNode parentNode, TagNode tagNode);

    /**
     * Action to be performed on single ContentToken in the tree
     * @param parentNode Parent of tagNode
     * @param contentToken content token visited
     * @return True if tree traversal should be continued, false if it has to stop.
     */
    public boolean visit(TagNode parentNode, ContentToken contentToken);

    /**
     * Action to be performed on single CommentToken in the tree
     * @param parentNode Parent of tagNode
     * @param commentToken content token visited
     * @return True if tree traversal should be continued, false if it has to stop.
     */
    public boolean visit(TagNode parentNode, CommentToken commentToken);

}