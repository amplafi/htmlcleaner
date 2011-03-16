package org.htmlcleaner;

/**
 * Used as base for different node checkers.
 */
public interface ITagNodeCondition {
    public boolean satisfy(TagNode tagNode);
}