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
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

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
 *
 *   // serialize a node to a file, output stream, DOM, JDom...
 *   new XXXSerializer(props).writeXmlXXX(aNode, ...);
 *   myJDom = new JDomSerializer(props, true).createJDom(aNode);
 *   myDom = new DomSerializer(props, true).createDOM(aNode);
 * </xmp>
 *
 * Created by: Vladimir Nikic <br/>
 * Date: November, 2006
 */
public class HtmlCleaner {

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
        	Iterator<TagPos> it = list.iterator();
            while ( it.hasNext() ) {
            	TagPos curr = (TagPos) it.next();
            	if ( tags.contains(curr.name) ) {
            		return true;
            	}
            }


            return false;
        }
    }

    private CleanerProperties properties;

    // Seems unnecessary since CleanerProperties also has the same value ( possible confusion ).
    @Deprecated
    private ITagInfoProvider tagInfoProvider;

    private CleanerTransformations transformations;

    private transient OpenTags _openTags;
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
        this.tagInfoProvider = tagInfoProvider == null ? DefaultTagProvider.getInstance() : tagInfoProvider;
        this.properties = properties == null ? new CleanerProperties() : properties;
        this.properties.setTagInfoProvider(this.tagInfoProvider);
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
        StringBuffer content = Utils.readUrl(url, charset);
        Reader reader = new StringReader( content.toString() );
        return clean(reader);
    }

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
        _openTags = new OpenTags();
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
        while(markNodesToPrune(nodeList));

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

        return rootNode;
    }

	private boolean markNodesToPrune(List nodeList) {
	    boolean nodesPruned = false;
		for (Iterator iterator = nodeList.iterator(); iterator.hasNext();) {
			Object next = iterator.next();
			if(next instanceof TagNode && !pruneNodeSet.contains(next)){
    			TagNode node = (TagNode) next;
    			if(addIfNeededToPruneSet(node)) {
			        nodesPruned = true;
    			} else if (node.getChildren()!=null){
    				nodesPruned |= markNodesToPrune(node.getChildren());
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
//            List bodyChildren = this.bodyNode.getChildren();
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
            List bodyChildren = this.bodyNode.getChildren();
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
                String prefix = (String) iterator.next();
                String xmlnsAtt = "xmlns:" + prefix;
                if ( !atts.containsKey(xmlnsAtt) ) {
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
			Iterator<Entry < String, String >> it = attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> currEntry = (Map.Entry<String, String>) it.next();
				String attName = (String) currEntry.getKey();
				if ( !tagAttributes.containsKey(attName) ) {
					String attValue = (String) currEntry.getValue();
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
            return fatalTagName == null ? true : _openTags.tagExists(fatalTagName);
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
                    TagPos tagPos = _openTags.findTag(fatalTag);
                    if (tagPos != null) {
                        fatalTagPositon = tagPos.position;
                    }
                }

	    		// iterates through the list of open tags from the end and check if there is some higher
	    		ListIterator it = _openTags.list.listIterator( _openTags.list.size() );
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
        TagPos last = _openTags.getLastTagPos();
        if (last != null) {
			 if (last.info != null) {
                 return last.info.allowsItem(token);
			 }
		}

		return true;
    }

    private void saveToLastOpenTag(List nodeList, Object tokenToAdd) {
        TagPos last = _openTags.getLastTagPos();
        if ( last != null && last.info != null && last.info.isIgnorePermitted() ) {
            return;
        }

        TagPos rubbishPos = _openTags.findTagToPlaceRubbish();
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
	void makeTree(List nodeList, ListIterator nodeIterator) {
		// process while not reach the end of the list
		while ( nodeIterator.hasNext() ) {
			BaseToken token = (BaseToken) nodeIterator.next();

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
					TagPos matchingPosition = _openTags.findTag(tagName);

                    if (matchingPosition != null) {
                        //open tag found.. closing the node.. this will add all 
                        //the nodes between open and end tokens to the childen list of the tag node. 
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
                    } else if (tag != null) {
                        //no open tags found for the ending. Possibly the open tag was closed due to one
                        //of his breaking children (i.e. table placed into p tag).
                        nodeIterator.previous();
                        
                        //going back until start or any tag node, skipping all the content nodes and null elements
                        //since they're cannot break our tag.
                        if(!nodeIterator.hasPrevious()){
                            //start of list reached -- no breaking tag exist, falling back.
                            nodeIterator.next();
                            continue;
                        }
                        Object previous = nodeIterator.previous();
                        //skipping all the content
                        while (nodeIterator.hasPrevious() && (previous instanceof ContentToken || previous == null)) {
                            previous = nodeIterator.previous();
                        }
                        if (previous instanceof TagToken) {
                            // TODO: pull out into a method to make it easier to document.
                            /* Tests that tag closed due to one of its children (when the child tag is not allowed to be inside parent) is then
                            * reopened.
                            * Examples:
                            * <pre>
                            * <div><p>text1<table><tr><td>text2</td></tr></table>text3</p></div>
                            * </pre>
                            * table is not allowed inside a <p> most browsers handle this by placing the table close to line before and line after and in general allowing it.
                            * 
                            * Cleaning here normally would result in :
                            * <pre>
                            * <div><p>text1<table><tr><td>text2</td></tr></table>text3</div>
                            * </pre>
                            * 'text3' is no longer inside the original element type ( 'p' ). Instead 'text3' is now within a 'div'. 
                            * text3 would no longer be styled correctly.
                            * 
                            * A more correct result is:
                            * <pre>
                            * <div><p>text1<table><tr><td>text2</td></tr></table><p>text3</p></div>
                            * </pre>
                            */ 
                            //tag node found
                            TagToken prevToken = (TagToken) previous;
                            TagInfo prevInfo = getTagInfoProvider().getTagInfo(prevToken.getName());
                            if (prevInfo.isMustCloseTag(tag)) {
                                //current tag was closed due to the node found, thus we need to reopen it
                                //after the node..
                                nodeIterator.next();
                                TagNode newStart = new TagNode(tagName);
                                nodeIterator.add(newStart);
                                _openTags.addTag(tag.getName(), nodeIterator.previousIndex());
                                //..and the properly close to form correct tree
                                closeSnippet(nodeList, _openTags.findTag(tagName), token);
                                skipTillToken(nodeIterator, token);
                                nodeIterator.set(null);
                            } else {
                                skipTillToken(nodeIterator, token);
                            }
                        } else {
                            skipTillToken(nodeIterator, token);
                        }
                    }
                }
			} else if ( isStartToken(token) ) {
                TagNode startTagToken = (TagNode) token;
				String tagName = startTagToken.getName();
				TagInfo tag = getTagInfoProvider().getTagInfo(tagName);

                TagPos lastTagPos = _openTags.isEmpty() ? null : _openTags.getLastTagPos();
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
                } else if ( (tag == null && properties.isOmitUnknownTags()) || (tag != null && tag.isDeprecated() && properties.isOmitDeprecatedTags()) ) {
                    nodeIterator.set(null);
                // if current tag is unknown and last open tag doesn't allow any other tags in its body
                } else if ( tag == null && lastTagInfo != null && !lastTagInfo.allowsAnything() ) {
                    closeSnippet(nodeList, lastTagPos, startTagToken);
                    nodeIterator.previous();
                } else if ( tag != null && tag.hasPermittedTags() && _openTags.someAlreadyOpen(tag.getPermittedTags()) ) {
                	nodeIterator.set(null);
                // if tag that must be unique, ignore this occurence
                } else if ( tag != null && tag.isUnique() && _openTags.tagEncountered(tagName) ) {
                	nodeIterator.set(null);
                // if there is no required outer tag without that this open tag is ignored
                } else if ( !isFatalTagSatisfied(tag) ) {
					nodeIterator.set(null);
                // if there is no required parent tag - it must be added before this open tag
                } else if ( mustAddRequiredParent(tag) ) {
					String requiredParent = tag.getRequiredParent();
					TagNode requiredParentStartToken = newTagNode(requiredParent);
					requiredParentStartToken.setAutoGenerated(true);
					nodeIterator.previous();
					nodeIterator.add(requiredParentStartToken);
					nodeIterator.previous();
                // if last open tag has lower presidence then this, it must be closed
                } else if ( tag != null && lastTagPos != null && tag.isMustCloseTag(lastTagInfo) ) {
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
				// if this open tag is not allowed inside last open tag, then it must be moved to the place where it can be
                } else if ( !isAllowedInLastOpenTag(token) ) {
                    saveToLastOpenTag(nodeList, token);
                    nodeIterator.set(null);
				// if it is known HTML tag but doesn't allow body, it is immediately closed
                } else if ( tag != null && !tag.allowsBody() ) {
					TagNode newTagNode = createTagNode(startTagToken);
                    addPossibleHeadCandidate(tag, newTagNode);
                    nodeIterator.set(newTagNode);
				// default case - just remember this open tag and go further
                } else {
                    _openTags.addTag( tagName, nodeIterator.previousIndex() );
                }
			} else {
				if ( !isAllowedInLastOpenTag(token) ) {
                    saveToLastOpenTag(nodeList, token);
                    nodeIterator.set(null);
				}
			}
		}
    }

    private void skipTillToken(ListIterator nodeIterator, BaseToken token) {
        while (nodeIterator.next() != token);
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
				if (child instanceof ContentToken) {
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

	private List closeSnippet(List nodeList, TagPos tagPos, Object toNode) {
		List closed = new ArrayList();
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
            		OpenTags prevOpenTags = _openTags;
            		_openTags = new OpenTags();
            		makeTree(itemsToMove, itemsToMove.listIterator(0));
                    closeAll(itemsToMove);
                    startTagToken.setItemsToMove(null);
                    _openTags = prevOpenTags;
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

                _openTags.removeTag( newTagNode.getName() );
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
        TagPos firstTagPos = _openTags.findFirstTagPos();
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
        return tagInfoProvider;
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
            String content = new SimpleXmlSerializer(properties).getXmlAsString(node);
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
            StringBuffer html = new StringBuffer();
            html.append("<" + nodeName + " marker=''>");
            html.append(content);
            html.append("</" + nodeName + ">");
            TagNode parent = node.getParent();
            while (parent != null) {
                String parentName = parent.getName();
                html.insert(0, "<" + parentName + ">");
                html.append("</" + parentName + ">");
                parent = parent.getParent();
            }

            TagNode rootNode = clean( html.toString() );
            TagNode cleanedNode = rootNode.findElementHavingAttribute("marker", true);
            if (cleanedNode != null) {
                node.setChildren( cleanedNode.getChildren() );
            }
        }
    }
    /**
     * @param transInfos
     */
    public void initCleanerTransformations(Map transInfos) {
        transformations = new CleanerTransformations(transInfos);
    }
}