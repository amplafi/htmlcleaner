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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.htmlcleaner.audit.ErrorType;
import org.htmlcleaner.conditional.ITagNodeCondition;

/**
 * Main HtmlCleaner class.
 *
 * <p>It represents public interface to the user. It's task is to call tokenizer with
 * specified source HTML, traverse list of produced token list and create internal
 * object model. It also offers a set of methods to write resulting XML to string,
 * file or any output stream.</p>
 * <p>Typical usage is the following:</p>
 *
 * <xmp>
 *    // create an instance of HtmlCleaner
 *   HtmlCleaner cleaner = new HtmlCleaner();
 *
 *   // take default cleaner properties
 *   CleanerProperties props = cleaner.getProperties();
 *
 *   // customize cleaner's behavior with property setters
 *   props.setXXX(...);
 *
 *   // Clean HTML taken from simple string, file, URL, input stream,
 *   // input source or reader. Result is root node of created
 *   // tree-like structure. Single cleaner instance may be safely used
 *   // multiple times.
 *   TagNode node = cleaner.clean(...);
 *
 *   // optionally find parts of the DOM or modify some nodes
 *   TagNode[] myNodes = node.getElementsByXXX(...);
 *   // and/or
 *   Object[] myNodes = node.evaluateXPath(xPathExpression);
 *   // and/or
 *   aNode.removeFromTree();
 *   // and/or
 *   aNode.addAttribute(attName, attValue);
 *   // and/or
 *   aNode.removeAttribute(attName, attValue);
 *   // and/or
 *   cleaner.setInnerHtml(aNode, htmlContent);
 *   // and/or do some other tree manipulation/traversal
 *
 *   // serialize a node to a file, output stream, DOM, JDom...
 *   new XXXSerializer(props).writeXmlXXX(aNode, ...);
 *   myJDom = new JDomSerializer(props, true).createJDom(aNode);
 *   myDom = new DomSerializer(props, true).createDOM(aNode);
 * </xmp>
 */
public class HtmlCleaner {

    /**
     * Marker attribute added to aid with part of the cleaning process.
     * TODO: a non-intrusive way of doing this that does not involve modifying the source html
     */
    private static final String MARKER_ATTRIBUTE = "htmlcleaner_marker";
    /**
     * Contains information about single open tag
     */
    private class TagPos {
		private int position;
		private String name;
		private TagInfo info;

		TagPos(int position, String name) {
			this.position = position;
			this.name = name;
            this.info = getTagInfoProvider().getTagInfo(name);
        }
	}

    /**
     * Contains information about nodes that were closed due to their child nodes.
     * i.e. if 'p' tag was closed due to 'table' child tag.
     *
     * @author Konstantin Burov
     *
     */
    private static class ChildBreaks{
        private Stack < TagPos> closedByChildBreak = new Stack < TagPos >();
        private Stack < TagPos > breakingTags = new Stack < TagPos >();

        /**
         * Adds the break info to the top of the stacks.
         *
         * @param closedPos - position of the tag that was closed due to incorrect child
         * @param breakPos - position of the child that has broken its parent
         */
        public void addBreak(TagPos closedPos, TagPos breakPos){
            closedByChildBreak.add(closedPos);
            breakingTags.add(breakPos);
        }

        public boolean isEmpty() {
            return closedByChildBreak.isEmpty();
        }

        /**
         * @return name of the last children tag that has broken its parent.
         */
        public String getLastBreakingTag() {
            return breakingTags.peek().name;
        }

        /**
         * pops out latest broken tag position.
         *
         * @return tag pos of the last parent that was broken.
         */
        public TagPos pop() {
            breakingTags.pop();
            return closedByChildBreak.pop();
        }

        /**
         * @return position of the last tag that has broken its parent. -1 if no such tag found.
         */
        public int getLastBreakingTagPosition() {
            return breakingTags.isEmpty()?-1:breakingTags.peek().position;
        }
    }

    /**
     * Class that contains information and methods for managing list of open,
     * but unhandled tags.
     */
    private class OpenTags {
        private List<TagPos> list = new ArrayList<TagPos>();
        private TagPos last;
        private Set<String> set = new HashSet<String>();

        private boolean isEmpty() {
            return list.isEmpty();
        }

        private void addTag(String tagName, int position) {
            last = new TagPos(position, tagName);
            list.add(last);
            set.add(tagName);
        }

        private void removeTag(String tagName) {
            ListIterator<TagPos> it = list.listIterator( list.size() );
            while ( it.hasPrevious() ) {
                TagPos currTagPos = it.previous();
                if (tagName.equals(currTagPos.name)) {
                    it.remove();
                    break;
                }
            }

            last =  list.isEmpty() ? null : (TagPos) list.get( list.size() - 1 );
        }

        private TagPos findFirstTagPos() {
            return list.isEmpty() ? null : (TagPos) list.get(0);
        }

        private TagPos getLastTagPos() {
            return last;
        }

        private TagPos findTag(String tagName) {
            if (tagName != null) {
                ListIterator<TagPos> it = list.listIterator(list.size());
                String fatalTag = null;
                TagInfo fatalInfo = getTagInfoProvider().getTagInfo(tagName);
                if (fatalInfo != null) {
                    fatalTag = fatalInfo.getFatalTag();
                }

                while (it.hasPrevious()) {
                    TagPos currTagPos = it.previous();
                    if (tagName.equals(currTagPos.name)) {
                        return currTagPos;
                    } else if (fatalTag != null && fatalTag.equals(currTagPos.name)) {
                        // do not search past a fatal tag for this tag
                        return null;
                    }
                }
            }

            return null;
        }

        private boolean tagExists(String tagName) {
            TagPos tagPos = findTag(tagName);
            return tagPos != null;
        }

        private TagPos findTagToPlaceRubbish() {
            TagPos result = null, prev = null;

            if ( !isEmpty() ) {
                ListIterator<TagPos> it = list.listIterator( list.size() );
                while ( it.hasPrevious() ) {
                    result = it.previous();
                    if ( result.info == null || result.info.allowsAnything() ) {
                    	if (prev != null) {
                            return prev;
                        }
                    }
                    prev = result;
                }
            }

            return result;
        }

        private boolean tagEncountered(String tagName) {
        	return set.contains(tagName);
        }

        /**
         * Checks if any of tags specified in the set are already open.
         * @param tags
         */
        private boolean someAlreadyOpen(Set<String> tags) {
        	for (TagPos curr : list) {
            	if ( tags.contains(curr.name) ) {
            		return true;
            	}
            }
            return false;
        }
    }

    private CleanerProperties properties;

    private CleanerTransformations transformations;

    private transient Stack<NestingState> nestingStates = new Stack<NestingState>();
    private transient boolean _headOpened;
    private transient boolean _bodyOpened;
    private transient Set<TagNode> _headTags = new LinkedHashSet<TagNode>();
    private Set<String> allTags = new TreeSet<String>();

    private TagNode htmlNode;
    private TagNode bodyNode;
    private TagNode headNode;
    private TagNode rootNode;

    private Set<TagNode> pruneNodeSet = new HashSet<TagNode>();

    private Set<ITagNodeCondition> pruneTagSet;

    private Set<ITagNodeCondition> allowTagSet;

    /**
     * Constructor - creates cleaner instance with default tag info provider and default properties.
     */
    public HtmlCleaner() {
        this(null, null);
    }

    /**
     * Constructor - creates the instance with specified tag info provider and default properties
     * @param tagInfoProvider Provider for tag filtering and balancing
     */
    public HtmlCleaner(ITagInfoProvider tagInfoProvider) {
        this(tagInfoProvider, null);
    }

    /**
     * Constructor - creates the instance with default tag info provider and specified properties
     * @param properties Properties used during parsing and serializing
     */
    public HtmlCleaner(CleanerProperties properties) {
        this(null, properties);
    }

    /**
	 * Constructor - creates the instance with specified tag info provider and specified properties
	 * @param tagInfoProvider Provider for tag filtering and balancing
	 * @param properties Properties used during parsing and serializing
	 */
	public HtmlCleaner(ITagInfoProvider tagInfoProvider, CleanerProperties properties) {
        this.properties = properties == null ? new CleanerProperties() : properties;
        this.properties.setTagInfoProvider(tagInfoProvider == null ? DefaultTagProvider.INSTANCE : tagInfoProvider);
	}

    public TagNode clean(String htmlContent) {
        try {
            return clean( new StringReader(htmlContent) );
        } catch (IOException e) {
            // should never happen because reading from StringReader
            throw new HtmlCleanerException(e);
        }
    }

    public TagNode clean(File file, String charset) throws IOException {
        FileInputStream in = new FileInputStream(file);
        Reader reader = new InputStreamReader(in, charset);
        return clean(reader);
    }

    public TagNode clean(File file) throws IOException {
        return clean(file, properties.getCharset());
    }

    public TagNode clean(URL url, String charset) throws IOException {
        CharSequence content = Utils.readUrl(url, charset);
        Reader reader = new StringReader( content.toString() );
        return clean(reader);
    }
    /**
     * Creates instance from the content downloaded from specified URL.
     * HTML encoding is resolved following the attempts in the sequence:
     * 1. reading Content-Type response header, 2. Analyzing META tags at the
     * beginning of the html, 3. Using platform's default charset.
     * @param url
     * @return
     * @throws IOException
     */
    public TagNode clean(URL url) throws IOException {
        return clean(url, properties.getCharset());
    }

    public TagNode clean(InputStream in, String charset) throws IOException {
        return clean( new InputStreamReader(in, charset) );
    }

    public TagNode clean(InputStream in) throws IOException {
        return clean(in, properties.getCharset());
    }

    /**
     * Basic version of the cleaning call.
     * @param reader
     * @return An instance of TagNode object which is the root of the XML tree.
     * @throws IOException
     */
    public TagNode clean(Reader reader) throws IOException {
        pushNesting();
        _headOpened = false;
        _bodyOpened = false;
        _headTags.clear();
        allTags.clear();
        this.pruneTagSet = new HashSet<ITagNodeCondition>(this.properties.getPruneTagSet());
        this.allowTagSet = new HashSet<ITagNodeCondition>(this.properties.getAllowTagSet());
        this.transformations = this.properties.getCleanerTransformations();
        this.pruneNodeSet.clear();

        htmlNode = this.newTagNode("html");
        bodyNode = this.newTagNode("body");
        headNode = this.newTagNode("head");
        rootNode = null;
        htmlNode.addChild(headNode);
        htmlNode.addChild(bodyNode);

        HtmlTokenizer htmlTokenizer = new HtmlTokenizer(this, reader);

		htmlTokenizer.start();

        List nodeList = htmlTokenizer.getTokenList();
        closeAll(nodeList);

        createDocumentNodes(nodeList);
        calculateRootNode( htmlTokenizer.getNamespacePrefixes() );

        // Some transitions on resulting html require us to have the tag tree structure.
        // i.e. if we want to clear insignificant <br> tags. Thus this place is best for
        // marking nodes to be pruned.
        while(markNodesToPrune(nodeList)) {
			// do them all
		}

        // if there are some nodes to prune from tree
        if ( pruneNodeSet != null && !pruneNodeSet.isEmpty() ) {
            Iterator<TagNode> iterator = pruneNodeSet.iterator();
            while (iterator.hasNext()) {
                TagNode tagNode = iterator.next();
                TagNode parent = tagNode.getParent();
                if (parent != null) {
                    parent.removeChild(tagNode);
                }
            }
        }

        rootNode.setDocType( htmlTokenizer.getDocType() );
        popNesting();
        return rootNode;
    }

	private boolean markNodesToPrune(List nodeList) {
	    boolean nodesPruned = false;
		for (Object next :nodeList) {
			if(next instanceof TagNode && !pruneNodeSet.contains(next)){
    			TagNode node = (TagNode) next;
    			if(addIfNeededToPruneSet(node)) {
			        nodesPruned = true;
    			} else if (!node.isEmpty()){
    				nodesPruned |= markNodesToPrune(node.getAllChildren());
    			}
    		}
    	}
		return nodesPruned;
	}
    /**
     * Assigns root node to internal variable and adds neccessery xmlns
     * attributes if cleaner if namespaces aware.
     * Root node of the result depends on parameter "omitHtmlEnvelope".
     * If it is set, then first child of the body will be root node,
     * or html will be root node otherwise.
     *
     * @param namespacePrefixes
     */
    private void calculateRootNode(Set<String> namespacePrefixes) {
        this.rootNode =  this.htmlNode;
// original behavior -- just take the first html element ignoring all other content, or later html elements.
//        if (properties.isOmitHtmlEnvelope()) {
//            List bodyChildren = this.bodyNode.getAllChildren();
//            if (bodyChildren != null) {
//                Iterator iterator = bodyChildren.iterator();
//                while (iterator.hasNext()) {
//                    Object currChild = iterator.next();
//                    // if found child that is tag itself, then return it
//                    if (currChild instanceof TagNode) {
//                        this.rootNode = (TagNode)currChild;
//                    }
//                }
//            }
//        }
        // new behavior -- wrap in null TagNode
        if (properties.isOmitHtmlEnvelope()) {
            List bodyChildren = this.bodyNode.getAllChildren();
            this.rootNode = new TagNode(null);
            if (bodyChildren != null) {
                for(Iterator iterator = bodyChildren.iterator(); iterator.hasNext(); ) {
                    Object currChild = iterator.next();
                    this.rootNode.addChild(currChild);
                }
            }
        }
        Map<String, String> atts = this.rootNode.getAttributes();

        if (properties.isNamespacesAware() && namespacePrefixes != null) {
            Iterator<String> iterator = namespacePrefixes.iterator();
            while (iterator.hasNext()) {
                String prefix = iterator.next();
                String xmlnsAtt = "xmlns:" + prefix;
                //
                // Don't include the XML NS
                // 
                if ( !atts.containsKey(xmlnsAtt) && !prefix.equals("xml")) {
                    this.rootNode.addAttribute(xmlnsAtt, prefix);
                }
            }
        }
    }

    /**
     * Add attributes from specified map to the specified tag.
     * If some attribute already exist it is preserved.
     * @param tag
     * @param attributes
     */
	private void addAttributesToTag(TagNode tag, Map<String, String> attributes) {
		if (attributes != null) {
			Map<String, String> tagAttributes = tag.getAttributes();
			for(Map.Entry< String, String > currEntry : attributes.entrySet()) {
				String attName = currEntry.getKey();
				if ( !tagAttributes.containsKey(attName) ) {
					String attValue = currEntry.getValue();
					tag.addAttribute(attName, attValue);
				}
			}
		}
	}

    /**
     * Checks if open fatal tag is missing if there is a fatal tag for
     * the specified tag.
     * @param tag
     */
    private boolean isFatalTagSatisfied(TagInfo tag) {
    	if (tag != null) {
            String fatalTagName = tag.getFatalTag();
            return fatalTagName == null ? true : getOpenTags().tagExists(fatalTagName);
    	}

    	return true;
    }

    /**
     * Check if specified tag requires parent tag, but that parent
     * tag is missing in the appropriate context.
     * @param tag
     */
    private boolean mustAddRequiredParent(TagInfo tag) {
    	if (tag != null) {
    		String requiredParent = tag.getRequiredParent();
    		if (requiredParent != null) {
	    		String fatalTag = tag.getFatalTag();
                int fatalTagPositon = -1;
                if (fatalTag != null) {
                    TagPos tagPos = getOpenTags().findTag(fatalTag);
                    if (tagPos != null) {
                        fatalTagPositon = tagPos.position;
                    }
                }

	    		// iterates through the list of open tags from the end and check if there is some higher
	    		ListIterator it = getOpenTags().list.listIterator( getOpenTags().list.size() );
	            while ( it.hasPrevious() ) {
	            	TagPos currTagPos = (TagPos) it.previous();
	            	if (tag.isHigher(currTagPos.name)) {
	            		return currTagPos.position <= fatalTagPositon;
	            	}
	            }

	            return true;
    		}
    	}

    	return false;
    }

    private TagNode newTagNode(String tagName) {
        TagNode tagNode = new TagNode(tagName);
        return tagNode;
    }

    private TagNode createTagNode(TagNode startTagToken) {
    	startTagToken.setFormed();
    	return startTagToken;
    }

    private boolean isAllowedInLastOpenTag(BaseToken token) {
        TagPos last = getOpenTags().getLastTagPos();
        if (last != null) {
			 if (last.info != null) {
                 return last.info.allowsItem(token);
			 }
		}

		return true;
    }

    private void saveToLastOpenTag(List nodeList, Object tokenToAdd) {
        TagPos last = getOpenTags().getLastTagPos();
        if ( last != null && last.info != null && last.info.isIgnorePermitted() ) {
            return;
        }

        TagPos rubbishPos = getOpenTags().findTagToPlaceRubbish();
        if (rubbishPos != null) {
    		TagNode startTagToken = (TagNode) nodeList.get(rubbishPos.position);
            startTagToken.addItemForMoving(tokenToAdd);
        }
    }

    private boolean isStartToken(Object o) {
    	return (o instanceof TagNode) && !((TagNode)o).isFormed();
    }

	/**
	 * This method generally mutates flattened list of tokens into tree structure.
	 *
	 * @param nodeList
	 * @param nodeIterator
	 */
	void makeTree(List nodeList, ListIterator<BaseToken> nodeIterator) {
		// process while not reach the end of the list
	    while ( nodeIterator.hasNext() ) {
	        BaseToken token = nodeIterator.next();

            if (token instanceof EndTagToken) {
				EndTagToken endTagToken = (EndTagToken) token;
				String tagName = endTagToken.getName();
				TagInfo tag = getTagInfoProvider().getTagInfo(tagName);

				if ( (tag == null && properties.isOmitUnknownTags()) || (tag != null && tag.isDeprecated() && properties.isOmitDeprecatedTags()) ) {
				    //tag is either unknown or deprecated, so we just prune the end token out
				    nodeIterator.set(null);
				} else if ( tag != null && !tag.allowsBody() ) {
				        //tag doesn't allow body, so end token is not needed
					nodeIterator.set(null);
				} else {
				        //trying to find corresponding opened tag for the end token
					TagPos matchingPosition = getOpenTags().findTag(tagName);

                    if (matchingPosition != null) {
                        //open tag found.. closing the node.. this will add all
                        //the nodes between open and end tokens to the children list of the tag node.
                        List closed = closeSnippet(nodeList, matchingPosition, endTagToken);
                        nodeIterator.set(null);
                        for (int i = closed.size() - 1; i >= 0; i--) {
                            TagNode closedTag = (TagNode) closed.get(i);

                            if ( i > 0 && tag != null && tag.isContinueAfter(closedTag.getName()) ) {
                                // even if pruned still want to allow a continuation.
                                // the nested tags that were also closed as part of the wrapping html closing.
                                // TODO: look at reversing hierarchy ( for example, "<b><i></b></i>" reverse to <i><b></b></i> )
                                TagNode cloned = closedTag.makeCopy();
                                cloned.setAutoGenerated(true);
                                nodeIterator.add( cloned );
                                nodeIterator.previous();
                            }
                        }
                        if(!getChildBreaks().isEmpty()){
                            while(matchingPosition.position < getChildBreaks().getLastBreakingTagPosition()){
                                //We're closing tag that is parent for the last closed by childbreak,
                                //thus we no longer need this info.
                                getChildBreaks().pop();
                            }
                        }
                        while( !getChildBreaks().isEmpty() && tagName.equals(getChildBreaks().getLastBreakingTag())
                        		&& matchingPosition.position == getChildBreaks().getLastBreakingTagPosition()){

                        	if(nodeList.get(getChildBreaks().closedByChildBreak.peek().position) != null) {
                        		//this tag has broken it's parent, thus the parent tag should be reopened.
                        		int position = getChildBreaks().pop().position;
                        		Object toReopen = nodeList.get(position);

                        		if(toReopen instanceof TagNode) {
                        			// normal case
                        			reopenBrokenNode(nodeIterator, (TagNode)toReopen);
                        		} else if (toReopen instanceof List) {
                        			// might happen with :
                        			// <table> -- opens table element
                        		    //  <br/> -- added to table's itemsToMove
                        		    //   <table> -- will close first table and result in List[br, table]
                        			//   </table> -- will try to reopen table, but table is now  a List

                        			List<TagNode> tagNodes = (List<TagNode>) toReopen;

                        			for(TagNode n : tagNodes) {
                        				nodeIterator.add(n);
                        				makeTree(nodeList, nodeList.listIterator(nodeList.size()-1));
                        			}
                        			// delete the elements from the previous position, we should not need them anymore
                        			nodeList.set(position, null);

                        		}

                        	} else {
                        		// Example of when it happens :
                        		// <li>Some incomplete li
                        		// <p><li>
                        		// When starting the second li tag, it will first close p, then the previous li.
                        		// li will then become the parent of p, and thus p will become null in the node list
                        		// so we cannot do much about that.
                        		// This means the HTML is messed up anyways, so we will not add a new p tag.
                        		getChildBreaks().pop();
                        	}

                        }
                    }
                }
			} else if ( isStartToken(token) ) {
                TagNode startTagToken = (TagNode) token;
				String tagName = startTagToken.getName();
				TagInfo tag = getTagInfoProvider().getTagInfo(tagName);

                TagPos lastTagPos = getOpenTags().isEmpty() ? null : getOpenTags().getLastTagPos();
                TagInfo lastTagInfo = lastTagPos == null ? null : getTagInfoProvider().getTagInfo(lastTagPos.name);

                // add tag to set of all tags
				allTags.add(tagName);

                // HTML open tag
                if ( "html".equals(tagName) ) {
					addAttributesToTag(htmlNode, startTagToken.getAttributes());
					nodeIterator.set(null);
                // BODY open tag
                } else if ( "body".equals(tagName) ) {
                    _bodyOpened = true;
                    addAttributesToTag(bodyNode, startTagToken.getAttributes());
					nodeIterator.set(null);
                // HEAD open tag
                } else if ( "head".equals(tagName) ) {
                    _headOpened = true;
                    addAttributesToTag(headNode, startTagToken.getAttributes());
					nodeIterator.set(null);
                // unknown HTML tag and unknown tags are not allowed
                } else if ( tag == null && properties.isOmitUnknownTags()) {
                    nodeIterator.set(null);
                    properties.fireUglyHtml(true, startTagToken, ErrorType.Unknown);
                } else if ( tag != null && tag.isDeprecated() && properties.isOmitDeprecatedTags()) {
                    nodeIterator.set(null);
                    properties.fireUglyHtml(true, startTagToken, ErrorType.Deprecated);
                // if current tag is unknown and last open tag doesn't allow any other tags in its body
                } else if ( tag == null && lastTagInfo != null && !lastTagInfo.allowsAnything() ) {
                    closeSnippet(nodeList, lastTagPos, startTagToken);
                    nodeIterator.previous();
                } else if ( tag != null && tag.hasPermittedTags() && getOpenTags().someAlreadyOpen(tag.getPermittedTags()) ) {
                	nodeIterator.set(null);
                // if tag that must be unique, ignore this occurence
                } else if ( tag != null && tag.isUnique() && getOpenTags().tagEncountered(tagName) ) {
                    nodeIterator.set(null);
                    properties.fireHtmlError(true, startTagToken, ErrorType.UniqueTagDuplicated);
                    // if there is no required outer tag without that this open tag is ignored
                } else if ( !isFatalTagSatisfied(tag) ) {
                    nodeIterator.set(null);
                    properties.fireHtmlError(true, startTagToken, ErrorType.FatalTagMissing);
                    // if there is no required parent tag - it must be added before this open tag
                } else if (mustAddRequiredParent(tag)) {
                    String requiredParent = tag.getRequiredParent();
                    TagNode requiredParentStartToken = newTagNode(requiredParent);
                    requiredParentStartToken.setAutoGenerated(true);
                    nodeIterator.previous();
                    nodeIterator.add(requiredParentStartToken);
                    nodeIterator.previous();
                    properties.fireHtmlError(true, startTagToken, ErrorType.RequiredParentMissing);
                    // if last open tag has lower presidence then this, it must be closed
                } else if ( tag != null && lastTagPos != null && tag.isMustCloseTag(lastTagInfo) ) {
                                        //since tag is closed earlier due to incorrect child tag, we store this info
                                        //to reopen it later, on the child close.
                                        getChildBreaks().addBreak(lastTagPos, new TagPos(nodeIterator.previousIndex(), tag.getName()));
                                        boolean certainty = startTagToken.hasAttribute("id") ? false : true;
                                        properties.fireHtmlError(certainty, (TagNode)nodeList.get(lastTagPos.position), ErrorType.UnpermittedChild);
                                        List closed = closeSnippet(nodeList, lastTagPos, startTagToken);
					int closedCount = closed.size();

					// it is needed to copy some tags again in front of current, if there are any
					if ( tag.hasCopyTags() && closedCount > 0 ) {
						// first iterates over list from the back and collects all start tokens
						// in sequence that must be copied
						ListIterator closedIt = closed.listIterator(closedCount);
						List toBeCopied = new ArrayList();
						while (closedIt.hasPrevious()) {
							TagNode currStartToken = (TagNode) closedIt.previous();
							if ( tag.isCopy(currStartToken.getName()) ) {
								toBeCopied.add(0, currStartToken);
							} else {
								break;
							}
						}

						if (toBeCopied.size() > 0) {
							Iterator copyIt = toBeCopied.iterator();
							while (copyIt.hasNext()) {
								TagNode currStartToken = (TagNode) copyIt.next();
								nodeIterator.add( currStartToken.makeCopy() );
							}

                            // back to the previous place, before adding new start tokens
							for (int i = 0; i < toBeCopied.size(); i++) {
								nodeIterator.previous();
							}
                        }
					}

                    nodeIterator.previous();
                } else if ( !isAllowedInLastOpenTag(token) ) {
                    // if this open tag is not allowed inside last open tag, then it must be moved to the place where it can be
                    saveToLastOpenTag(nodeList, token);
                    nodeIterator.set(null);
                } else if ( tag != null && !tag.allowsBody() ) {
                    // if it is known HTML tag but doesn't allow body, it is immediately closed
					TagNode newTagNode = createTagNode(startTagToken);
                    addPossibleHeadCandidate(tag, newTagNode);
                    nodeIterator.set(newTagNode);
				// default case - just remember this open tag and go further
                } else {
                    getOpenTags().addTag( tagName, nodeIterator.previousIndex() );
                }
			} else {
				if (_headOpened && !_bodyOpened && properties.isKeepWhitespaceAndCommentsInHead()) {
					if (token instanceof CommentNode) {
						if (getOpenTags().getLastTagPos()==null) {
							_headTags.add(new ProxyTagNode((CommentNode)token, bodyNode));
						}
					} else if (token instanceof ContentNode) {
						ContentNode contentNode = (ContentNode)token;
						if (contentNode.isBlank()) {
							BaseToken lastTok = (BaseToken)nodeList.get(nodeList.size()-1);
							if (lastTok==token) {
								_headTags.add(new ProxyTagNode(contentNode, bodyNode));
							}
						}
					}
				}

				if ( !isAllowedInLastOpenTag(token) ) {
                    saveToLastOpenTag(nodeList, token);
                    nodeIterator.set(null);
				}
			}
		}
    }

	private void reopenBrokenNode(ListIterator<BaseToken> nodeIterator, TagNode toReopen) {
		TagNode closedByPresidence = toReopen;
		TagNode copy = closedByPresidence.makeCopy();
		copy.setAutoGenerated(true);
		copy.removeAttribute("id");
		nodeIterator.add(copy);
		getOpenTags().addTag(closedByPresidence.getName(), nodeIterator.previousIndex());
	}

	/**
	 *
	 * @param startTagToken
	 * @return true if no id attribute or class attribute
	 */
    protected boolean isRemovingNodeReasonablySafe(TagNode startTagToken) {
        return !startTagToken.hasAttribute("id") && !startTagToken.hasAttribute("name") && !startTagToken.hasAttribute("class");
    }


	private void createDocumentNodes(List listNodes) {
		Iterator it = listNodes.iterator();
        while (it.hasNext()) {
            Object child = it.next();

            if (child == null) {
            	continue;
            }

			boolean toAdd = true;

            if (child instanceof TagNode) {
                TagNode node = (TagNode) child;
                TagInfo tag = getTagInfoProvider().getTagInfo( node.getName() );
                addPossibleHeadCandidate(tag, node);
			} else {
				if (child instanceof ContentNode) {
					toAdd = !"".equals(child.toString());
				}
			}

			if (toAdd) {
				bodyNode.addChild(child);
			}
        }

        // move all viable head candidates to head section of the tree
        Iterator headIterator = _headTags.iterator();
        while (headIterator.hasNext()) {
        	TagNode headCandidateNode = (TagNode) headIterator.next();

            // check if this node is already inside a candidate for moving to head
            TagNode parent = headCandidateNode.getParent();
            boolean toMove = true;
            while (parent != null) {
                if ( _headTags.contains(parent) ) {
                    toMove = false;
                    break;
                }
                parent = parent.getParent();
            }

            if (toMove) {
                headCandidateNode.removeFromTree();
                headNode.addChild(headCandidateNode);
            }
        }
    }

	/**
	 * Forced closing
	 * @param nodeList
	 * @param tagPos
	 * @param toNode
	 * @return
	 */
	private List<TagNode> closeSnippet(List nodeList, TagPos tagPos, Object toNode) {
		List<TagNode> closed = new ArrayList<TagNode>();
		ListIterator it = nodeList.listIterator(tagPos.position);

		TagNode tagNode = null;
		Object item = it.next();
		boolean isListEnd = false;

		while ( (toNode == null && !isListEnd) || (toNode != null && item != toNode) ) {
			if ( isStartToken(item) ) {
                TagNode startTagToken = (TagNode) item;
                closed.add(startTagToken);
                List itemsToMove = startTagToken.getItemsToMove();
                if (itemsToMove != null) {
            		pushNesting();
            		makeTree(itemsToMove, itemsToMove.listIterator(0));
                    closeAll(itemsToMove);
                    startTagToken.setItemsToMove(null);
                    popNesting();
                }

                TagNode newTagNode = createTagNode(startTagToken);
                TagInfo tag = getTagInfoProvider().getTagInfo( newTagNode.getName() );
                addPossibleHeadCandidate(tag, newTagNode);
                if (tagNode != null) {
					tagNode.addChildren(itemsToMove);
                    tagNode.addChild(newTagNode);
                    it.set(null);
                } else {
                	if (itemsToMove != null) {
                		itemsToMove.add(newTagNode);
                		it.set(itemsToMove);
                	} else {
                		it.set(newTagNode);
                	}
                }

                getOpenTags().removeTag( newTagNode.getName() );
                tagNode = newTagNode;
            } else {
            	if (tagNode != null) {
            		it.set(null);
            		if (item != null) {
            			tagNode.addChild(item);
                    }
                }
            }

			if ( it.hasNext() ) {
				item = it.next();
			} else {
				isListEnd = true;
			}
		}
		return closed;
    }

    /**
     * Close all unclosed tags if there are any.
     */
    private void closeAll(List nodeList) {
        TagPos firstTagPos = getOpenTags().findFirstTagPos();
        for (TagPos pos : getOpenTags().list) {
            properties.fireHtmlError(true, (TagNode)nodeList.get(pos.position), ErrorType.UnclosedTag);
        }
        if (firstTagPos != null) {
            closeSnippet(nodeList, firstTagPos, null);
        }
    }

    /**
     * Checks if specified tag with specified info is candidate for moving to head section.
     * @param tagInfo
     * @param tagNode
     */
    private void addPossibleHeadCandidate(TagInfo tagInfo, TagNode tagNode) {
        if (tagInfo != null && tagNode != null) {
            if ( tagInfo.isHeadTag() || (tagInfo.isHeadAndBodyTag() && _headOpened && !_bodyOpened) ) {
                _headTags.add(tagNode);
            }
        }
    }

    public CleanerProperties getProperties() {
        return properties;
    }

    public Set<ITagNodeCondition> getPruneTagSet() {
        return pruneTagSet;
    }

    public Set<ITagNodeCondition> getAllowTagSet() {
        return allowTagSet;
    }

    public void addPruneNode(TagNode node) {
    	node.setPruned(true);
        this.pruneNodeSet.add(node);
    }

    private boolean addIfNeededToPruneSet(TagNode tagNode) {
        if ( pruneTagSet != null ) {
            for(ITagNodeCondition condition: pruneTagSet) {
                if ( condition.satisfy(tagNode)) {
                    addPruneNode(tagNode);
                    properties.fireConditionModification(condition, tagNode);
                    return true;
                }
            }
        }

        if ( allowTagSet != null && !allowTagSet.isEmpty() ) {
            for(ITagNodeCondition condition: allowTagSet) {
                if ( condition.satisfy(tagNode)) {
                    return false;
                }
            }
            if (!tagNode.isAutoGenerated()) {
                properties.fireUserDefinedModification(true, tagNode, ErrorType.NotAllowedTag);
            }
            addPruneNode(tagNode);
            return true;
        }
        return false;
    }

    public Set<String> getAllTags() {
		return allTags;
	}

    /**
     * @return ITagInfoProvider instance for this HtmlCleaner
     */
    public ITagInfoProvider getTagInfoProvider() {
        return this.properties.getTagInfoProvider();
    }

    /**
     * @return Transformations defined for this instance of cleaner
     */
    public CleanerTransformations getTransformations() {
        return transformations;
    }

    /**
     * For the specified node, returns it's content as string.
     * @param node
     * @return node's content as string
     */
    public String getInnerHtml(TagNode node) {
        if (node != null) {
            String content = new SimpleXmlSerializer(properties).getAsString(node);
            int index1 = content.indexOf("<" + node.getName());
            index1 = content.indexOf('>', index1 + 1);
            int index2 = content.lastIndexOf('<');
            return index1 >= 0 && index1 <= index2 ? content.substring(index1 + 1, index2) : null;
        } else {
            throw new HtmlCleanerException("Cannot return inner html of the null node!");
        }
    }

    /**
     * For the specified tag node, defines it's html content. This causes cleaner to
     * reclean given html portion and insert it inside the node instead of previous content.
     * @param node
     * @param content
     */
    public void setInnerHtml(TagNode node, String content) {
        if (node != null) {
            String nodeName = node.getName();
            StringBuilder html = new StringBuilder();
            html.append("<").append(nodeName).append(" " +MARKER_ATTRIBUTE +"=''>").append(content).append("</").append(nodeName).append(">");
            TagNode parent = node.getParent();
            while (parent != null) {
                String parentName = parent.getName();
                html.insert(0, "<" + parentName + ">");
                html.append("</").append(parentName).append(">");
                parent = parent.getParent();
            }

            TagNode innerRootNode = clean( html.toString() );
            TagNode cleanedNode = innerRootNode.findElementHavingAttribute(MARKER_ATTRIBUTE, true);
            if (cleanedNode != null) {
                node.setChildren( cleanedNode.getAllChildren() );
            }
        }
    }
    /**
     * @param transInfos
     */
    public void initCleanerTransformations(Map transInfos) {
        transformations = new CleanerTransformations(transInfos);
    }

	private OpenTags getOpenTags() {
		return this.nestingStates.peek().getOpenTags();
	}

	private ChildBreaks getChildBreaks() {
		return this.nestingStates.peek().getChildBreaks();
	}

	// TODO: better name
	private NestingState pushNesting() {
		return this.nestingStates.push(new NestingState());
	}
	private NestingState popNesting() {
		return this.nestingStates.pop();
	}
	protected class NestingState {
		private OpenTags openTags = new OpenTags();
		private ChildBreaks childBreaks = new ChildBreaks();

		public OpenTags getOpenTags() {
			return this.openTags;
		}
		public ChildBreaks getChildBreaks() {
			return this.childBreaks;
		}
	}
}