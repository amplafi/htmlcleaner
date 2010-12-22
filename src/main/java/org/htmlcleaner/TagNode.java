/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.
	
    Redistribution and use of this software in source and binary forms, 
    with or without modification, are permitted provided that the following 
    conditions are met:
	
    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.
	
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.
	
    * The name of HtmlCleaner may not be used to endorse or promote 
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
    POSSIBILITY OF SUCH DAMAGE.
	
    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "HtmlCleaner" in the
    subject line.
*/

package org.htmlcleaner;

import java.io.*;
import java.util.*;

/**
 * <p>
 *      XML node tag - basic node of the cleaned HTML tree. At the same time, it represents start tag token
 *      after HTML parsing phase and before cleaning phase. After cleaning process, tree structure remains
 *      containing tag nodes (TagNode class), content (text nodes - ContentNode), comments (CommentNode)
 *      and optionally doctype node (DoctypeToken).
 * </p>
 */
public class TagNode extends TagToken implements HtmlNode {

    /**
     * Used as base for different node checkers.
     */
    public interface ITagNodeCondition {
        public boolean satisfy(TagNode tagNode);
    }

    /**
     * All nodes.
     */
    public class TagAllCondition implements ITagNodeCondition {
        public boolean satisfy(TagNode tagNode) {
            return true;
        }
    }

    /**
     * Checks if node has specified name.
     */
    public class TagNodeNameCondition implements ITagNodeCondition {
        private String name;

        public TagNodeNameCondition(String name) {
            this.name = name;
        }

        public boolean satisfy(TagNode tagNode) {
            return tagNode == null ? false : tagNode.name.equalsIgnoreCase(this.name);
        }
    }

    /**
     * Checks if node contains specified attribute.
     */
    public class TagNodeAttExistsCondition implements ITagNodeCondition {
        private String attName;

        public TagNodeAttExistsCondition(String attName) {
            this.attName = attName;
        }

        public boolean satisfy(TagNode tagNode) {
            return tagNode == null ? false : tagNode.attributes.containsKey( attName.toLowerCase() );
        }
    }

    /**
     * Checks if node has specified attribute with specified value.
     */
    public class TagNodeAttValueCondition implements ITagNodeCondition {
        private String attName;
        private String attValue;
        private boolean isCaseSensitive;

        public TagNodeAttValueCondition(String attName, String attValue, boolean isCaseSensitive) {
            this.attName = attName;
            this.attValue = attValue;
            this.isCaseSensitive = isCaseSensitive;
        }

        public boolean satisfy(TagNode tagNode) {
            if (tagNode == null || attName == null || attValue == null) {
                return false;
            } else {
                return isCaseSensitive ?
                        attValue.equals( tagNode.getAttributeByName(attName) ) :
                        attValue.equalsIgnoreCase( tagNode.getAttributeByName(attName) );
            }
        }
    }

    private TagNode parent = null; 
    private Map<String, String> attributes = new LinkedHashMap<String, String>();
    private List children = new ArrayList();
    private DoctypeToken docType = null;
    private Map<String, String> nsDeclarations = null;
    private List<BaseToken> itemsToMove = null;

    private transient boolean isFormed = false;


    public TagNode(String name) {
        super(name == null ? null : name.toLowerCase());
    }

    /**
     * Changes name of the tag
     * @param name
     * @return True if new name is valid, false otherwise
     */
    public boolean setName(String name) {
        if (Utils.isValidXmlIdentifier(name)) {
            this.name = name;
            return true;
        }

        return false;
    }

    /**
     * @param attName
     * @return Value of the specified attribute, or null if it this tag doesn't contain it. 
     */
    public String getAttributeByName(String attName) {
		return attName != null ? attributes.get(attName.toLowerCase()) : null;
	}

    /**
     * @return Map instance containing all attribute name/value pairs.
     */
    public Map<String, String> getAttributes() {
		return attributes;
	}

    /**
     * Checks existance of specified attribute.
     * @param attName
     */
    public boolean hasAttribute(String attName) {
        return attName != null ? attributes.containsKey(attName.toLowerCase()) : false;
    }

    /**
     * @deprecated Use setAttribute instead
     * Adds specified attribute to this tag or overrides existing one.
     * @param attName
     * @param attValue
     */
    @Deprecated
    public void addAttribute(String attName, String attValue) {
        setAttribute(attName, attValue);
    }

    /**
     * Adding new attribute ir overriding existing one.
     * @param attName
     * @param attValue
     */
    public void setAttribute(String attName, String attValue) {
        if ( attName != null && !"".equals(attName.trim()) ) {
            attName = attName.toLowerCase();
            if ("xmlns".equals(attName)) {
                addNamespaceDeclaration("", attValue);    
            } else if (attName.startsWith("xmlns:")) {
                addNamespaceDeclaration( attName.substring(6), attValue );
            } else {
                attributes.put(attName, attValue == null ? "" : attValue );
            }
        }
    }

    /**
     * Adds namespace declaration to the node
     * @param nsPrefix Namespace prefix
     * @param nsURI Namespace URI
     */
    public void addNamespaceDeclaration(String nsPrefix, String nsURI) {
        if (nsDeclarations == null) {
            nsDeclarations = new TreeMap<String, String>();
        }
        nsDeclarations.put(nsPrefix, nsURI);
    }

    /**
     * @return Map of namespace declarations for this node
     */
    public Map<String, String> getNamespaceDeclarations() {
        return nsDeclarations;
    }

    /**
     * Removes specified attribute from this tag.
     * @param attName
     */
    public void removeAttribute(String attName) {
        if ( attName != null && !"".equals(attName.trim()) ) {
            attributes.remove( attName.toLowerCase() );
        }
    }

    /**
     * @return List of children objects. During the cleanup process there could be different kind of
     * childern inside, however after clean there should be only TagNode instances.
     */
    public List getChildren() {
		return children;
	}

    /**
     * @return Whether this node has child elements or not.
     */
    public boolean hasChildren() {
        return children.size() > 0;
    }

    void setChildren(List children) {
        this.children = children;
    }

    public List getChildTagList() {
        List childTagList = new ArrayList();
        for (int i = 0; i < children.size(); i++) {
            Object item = children.get(i);
            if (item instanceof TagNode) {
                childTagList.add(item);
            }
        }

        return childTagList;
    }

    /**
     * @return An array of child TagNode instances.
     */
    public TagNode[] getChildTags() {
        List childTagList = getChildTagList();
        TagNode childrenArray[] = new TagNode[childTagList.size()];
        for (int i = 0; i < childTagList.size(); i++) {
            childrenArray[i] = (TagNode) childTagList.get(i);
        }

        return childrenArray;
    }

    /**
     * @return Text content of this node and it's subelements.
     */
    public StringBuffer getText() {
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < children.size(); i++) {
            Object item = children.get(i);
            if (item instanceof ContentNode) {
                text.append(item.toString());
            } else if (item instanceof TagNode) {
                StringBuffer subtext = ((TagNode)item).getText();
                text.append(subtext);
            }
        }

        return text;
    }

    /**
     * @return Parent of this node, or null if this is the root node.
     */
    public TagNode getParent() {
		return parent;
	}

    public DoctypeToken getDocType() {
        return docType;
    }

    public void setDocType(DoctypeToken docType) {
        this.docType = docType;
    }

    public void addChild(Object child) {
        if (child == null) {
            return;
        }
        if (child instanceof List) {
            addChildren( (List)child );
        } else {
            children.add(child);
            if (child instanceof TagNode) {
                TagNode childTagNode = (TagNode)child;
                childTagNode.parent = this;
            }
        }
    }

    /**
     * Add all elements from specified list to this node.
     * @param newChildren
     */
    public void addChildren(List newChildren) {
    	if (newChildren != null) {
    		Iterator it = newChildren.iterator();
    		while (it.hasNext()) {
    			Object child = it.next();
    			addChild(child);
    		}
    	}
    }

    /**
     * Finds first element in the tree that satisfy specified condition.
     * @param condition
     * @param isRecursive
     * @return First TagNode found, or null if no such elements.
     */
    private TagNode findElement(ITagNodeCondition condition, boolean isRecursive) {
        if (condition == null) {
            return null;
        }

        for (int i = 0; i < children.size(); i++) {
            Object item = children.get(i);
            if (item instanceof TagNode) {
                TagNode currNode = (TagNode) item;
                if ( condition.satisfy(currNode) ) {
                    return currNode;
                } else if (isRecursive) {
                    TagNode inner = currNode.findElement(condition, isRecursive);
                    if (inner != null) {
                        return inner;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get all elements in the tree that satisfy specified condition.
     * @param condition
     * @param isRecursive
     * @return List of TagNode instances with specified name.
     */
    private List getElementList(ITagNodeCondition condition, boolean isRecursive) {
        List result = new LinkedList();
        if (condition == null) {
            return result;
        }

        for (int i = 0; i < children.size(); i++) {
            Object item = children.get(i);
            if (item instanceof TagNode) {
                TagNode currNode = (TagNode) item;
                if ( condition.satisfy(currNode) ) {
                    result.add(currNode);
                }
                if (isRecursive) {
                    List innerList = currNode.getElementList(condition, isRecursive);
                    if (innerList != null && innerList.size() > 0) {
                        result.addAll(innerList);
                    }
                }
            }
        }

        return result;
    }

    /**
     * @param condition
     * @param isRecursive
     * @return The array of all subelemets that satisfy specified condition.
     */
    private TagNode[] getElements(ITagNodeCondition condition, boolean isRecursive) {
        final List list = getElementList(condition, isRecursive);
        TagNode array[] = new TagNode[ list == null ? 0 : list.size() ];
        for (int i = 0; i < list.size(); i++) {
            array[i] = (TagNode) list.get(i);
        }

        return array;
    }


    public List getAllElementsList(boolean isRecursive) {
        return getElementList( new TagAllCondition(), isRecursive );
    }

    public TagNode[] getAllElements(boolean isRecursive) {
        return getElements( new TagAllCondition(), isRecursive );
    }

    public TagNode findElementByName(String findName, boolean isRecursive) {
        return findElement( new TagNodeNameCondition(findName), isRecursive );
    }

    public List getElementListByName(String findName, boolean isRecursive) {
        return getElementList( new TagNodeNameCondition(findName), isRecursive );
    }

    public TagNode[] getElementsByName(String findName, boolean isRecursive) {
        return getElements( new TagNodeNameCondition(findName), isRecursive );
    }

    public TagNode findElementHavingAttribute(String attName, boolean isRecursive) {
        return findElement( new TagNodeAttExistsCondition(attName), isRecursive );
    }

    public List getElementListHavingAttribute(String attName, boolean isRecursive) {
        return getElementList( new TagNodeAttExistsCondition(attName), isRecursive );
    }

    public TagNode[] getElementsHavingAttribute(String attName, boolean isRecursive) {
        return getElements( new TagNodeAttExistsCondition(attName), isRecursive );
    }

    public TagNode findElementByAttValue(String attName, String attValue, boolean isRecursive, boolean isCaseSensitive) {
        return findElement( new TagNodeAttValueCondition(attName, attValue, isCaseSensitive), isRecursive );
    }

    public List getElementListByAttValue(String attName, String attValue, boolean isRecursive, boolean isCaseSensitive) {
        return getElementList( new TagNodeAttValueCondition(attName, attValue, isCaseSensitive), isRecursive );
    }

    public TagNode[] getElementsByAttValue(String attName, String attValue, boolean isRecursive, boolean isCaseSensitive) {
        return getElements( new TagNodeAttValueCondition(attName, attValue, isCaseSensitive), isRecursive );
    }

    /**
     * Evaluates XPath expression on give node. <br>
     * <em>
     *  This is not fully supported XPath parser and evaluator.
     *  Examples below show supported elements:
     * </em>
     * <code>
     * <ul>
     *      <li>//div//a</li>
     *      <li>//div//a[@id][@class]</li>
     *      <li>/body/*[1]/@type</li>
     *      <li>//div[3]//a[@id][@href='r/n4']</li>
     *      <li>//div[last() >= 4]//./div[position() = last()])[position() > 22]//li[2]//a</li>
     *      <li>//div[2]/@*[2]</li>
     *      <li>data(//div//a[@id][@class])</li>
     *      <li>//p/last()</li>
     *      <li>//body//div[3][@class]//span[12.2<position()]/@id</li>
     *      <li>data(//a['v' < @id])</li>
     * </ul>
     * </code>
     * @param xPathExpression
     * @return
     * @throws XPatherException
     */
    public Object[] evaluateXPath(String xPathExpression) throws XPatherException {
        return new XPather(xPathExpression).evaluateAgainstNode(this);
    }

    /**
     * Remove this node from the tree.
     * @return True if element is removed (if it is not root node).
     */
    public boolean removeFromTree() {
        if (parent != null) {
            boolean existed = parent.removeChild(this);
            parent = null;
            return existed;
        }
        return false;
    }

    /**
     * Remove specified child element from this node.
     * @param child
     * @return True if child object existed in the children list.
     */
    public boolean removeChild(Object child) {
        return this.children.remove(child);
    }

    /**
     * Removes all children (subelements and text content).
     */
    public void removeAllChildren() {
        this.children.clear();
    }

    /**
     * Replaces specified child node with specified replacement node.
     * @param childToReplace Child node to be replaced
     * @param replacement Replacement node
     */
    public void replaceChild(HtmlNode childToReplace, HtmlNode replacement) {
        if (replacement == null) {
            return;
        }
        ListIterator it = children.listIterator();
        while (it.hasNext()) {
            Object curr = it.next();
            if (curr == childToReplace) {
                it.set(replacement);
                break;
            }
        }
    }

    /**
     * @param child Child to find index of
     * @return Index of the specified child node inside this node's children, -1 if node is not the child 
     */
    public int getChildIndex(HtmlNode child) {
        int index = 0;
        for (Object curr: children) {
            if (curr == child) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Inserts specified node at specified position in array of children  
     * @param index
     * @param childToAdd
     */
    public void insertChild(int index, HtmlNode childToAdd) {
        children.add(index, childToAdd);
    }

    /**
     * Inserts specified node in the list of children before specified child
     * @param node Child before which to insert new node
     * @param nodeToInsert Node to be inserted at specified position
     */
    public void insertChildBefore(HtmlNode node, HtmlNode nodeToInsert) {
        int index = getChildIndex(node);
        if (index >= 0) {
            insertChild(index, nodeToInsert);
        }
    }

    /**
     * Inserts specified node in the list of children after specified child
     * @param node Child after which to insert new node
     * @param nodeToInsert Node to be inserted at specified position
     */
    public void insertChildAfter(HtmlNode node, HtmlNode nodeToInsert) {
        int index = getChildIndex(node);
        if (index >= 0) {
            insertChild(index + 1, nodeToInsert);
        }
    }

    void addItemForMoving(BaseToken item) {
    	if (itemsToMove == null) {
    		itemsToMove = new ArrayList<BaseToken>();
    	}
    	
    	itemsToMove.add(item);
    }
    
    List<BaseToken> getItemsToMove() {
		return itemsToMove;
	}

    void setItemsToMove(List<BaseToken> itemsToMove) {
        this.itemsToMove = itemsToMove;
    }

	boolean isFormed() {
		return isFormed;
	}

	void setFormed(boolean isFormed) {
		this.isFormed = isFormed;
	}

	void setFormed() {
		setFormed(true);
	}

    void transformAttributes(TagTransformation tagTrans) {
        boolean isPreserveSourceAtts = tagTrans.isPreserveSourceAttributes();
        boolean hasAttTransforms = tagTrans.hasAttributeTransformations();
        if ( hasAttTransforms || !isPreserveSourceAtts) {
            Map<String, String> newAttributes = isPreserveSourceAtts ? new LinkedHashMap<String, String>(attributes) : new LinkedHashMap<String, String>();
            if (hasAttTransforms) {
                Map map = tagTrans.getAttributeTransformations();
                Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String attName = (String) entry.getKey();
                    String template = (String) entry.getValue();
                    if (template == null) {
                        newAttributes.remove(attName);
                    } else {
                        String attValue = Utils.evaluateTemplate(template, attributes);
                        newAttributes.put(attName, attValue);
                    }
                }
            }
            this.attributes = newAttributes;
        }
    }

    /**
     * Traverses the tree and performs visitor's action on each node. It stops when it
     * finishes all the tree or when visitor returns false.
     * @param visitor TagNodeVisitor implementation
     */
    public void traverse(TagNodeVisitor visitor) {
        traverseInternally(visitor);
    }


    private boolean traverseInternally(TagNodeVisitor visitor) {
        if (visitor != null) {
            boolean hasParent = parent != null;
            boolean toContinue = visitor.visit(parent, this);

            if (!toContinue) {
                return false; // if visitor stops traversal
            } else if (hasParent && parent == null) {
                return true; // if this node is pruned from the tree during the visit, then don't go deeper
            }
            for (Object child: children.toArray()) {  // make an array to avoid ConcurrentModificationException when some node is cut 
                if (child instanceof TagNode) {
                    toContinue = ((TagNode)child).traverseInternally(visitor);
                } else if (child instanceof ContentNode) {
                    toContinue = visitor.visit(this, (ContentNode)child);
                } else if (child instanceof CommentNode) {
                    toContinue = visitor.visit(this, (CommentNode)child);
                }
                if (!toContinue) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Collect all prefixes in namespace declarations up the path to the document root from the specified node
     * @param prefixes Set of prefixes to be collected
     */
    void collectNamespacePrefixesOnPath(Set<String> prefixes) {
        Map<String, String> nsDeclarations = getNamespaceDeclarations();
        if (nsDeclarations != null) {
            for (String prefix: nsDeclarations.keySet()) {
                prefixes.add(prefix);
            }
        }
        if (parent != null) {
            parent.collectNamespacePrefixesOnPath(prefixes);
        }
    }

    String getNamespaceURIOnPath(String nsPrefix) {
        if (nsDeclarations != null) {
            for (Map.Entry<String, String> nsEntry: nsDeclarations.entrySet()) {
                String currName = nsEntry.getKey();
                if ( currName.equals(nsPrefix) || ("".equals(currName) && nsPrefix == null) ) {
                    return nsEntry.getValue();
                }
            }
        }
        if (parent != null) {
            return parent.getNamespaceURIOnPath(nsPrefix);
        }

        return null;
    }

    public void serialize(Serializer serializer, Writer writer) throws IOException {
    	serializer.serialize(this, writer);
    }
    
    TagNode makeCopy() {
    	TagNode copy = new TagNode(name);
        copy.attributes.putAll(attributes);
    	return copy;
    }

}