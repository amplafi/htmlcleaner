package org.htmlcleaner;

/**
 * Defines action to be performed on TagNodes
 */
public interface TagNodeVisitor {

    /**
     * Action to be performed on single TagNode in the tree
     * @return True if tree traversal should be continued, false if it has to stop.
     */
    public boolean visit(TagNode tagNode);

}