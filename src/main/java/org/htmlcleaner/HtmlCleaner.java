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
import java.net.URLConnection;
import java.util.*;

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
 *   // customize cleaner's behaviour with property setters
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

    public static final String DEFAULT_CHARSET = System.getProperty("file.encoding");

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
            this.info = tagInfoProvider.getTagInfo(name);
        }
	}

    /**
     * Class that contains information and mathods for managing list of open,
     * but unhandled tags.
     */
    private class OpenTags {
        private List<TagPos> list = new ArrayList<TagPos>();
        private TagPos last = null;
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

            last =  list.isEmpty() ? null : list.get( list.size() - 1 );
        }

        private TagPos findFirstTagPos() {
            return list.isEmpty() ? null : list.get(0);
        }

        private TagPos getLastTagPos() {
            return last;
        }

        private TagPos findTag(String tagName) {
            if (tagName != null) {
                ListIterator<TagPos> it = list.listIterator(list.size());
                String fatalTag = null;
                TagInfo fatalInfo = tagInfoProvider.getTagInfo(tagName);
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
        private boolean someAlreadyOpen(Set tags) {
        	Iterator<TagPos> it = list.iterator();
            while ( it.hasNext() ) {
            	TagPos curr = it.next();
            	if ( tags.contains(curr.name) ) {
            		return true;
            	}
            }


            return false;
        }
    }

    private class CleanTimeValues {
        private OpenTags _openTags;
        private boolean _headOpened = false;
        private boolean _bodyOpened = false;
        private Set _headTags = new LinkedHashSet();
        private Set allTags = new TreeSet();

        private TagNode htmlNode;
        private TagNode bodyNode;
        private TagNode headNode;
        private TagNode rootNode;

        private Set<String> pruneTagSet = new HashSet<String>();
        private Set<TagNode> pruneNodeSet = new HashSet<TagNode>();
    }

    private CleanerProperties properties;

    private ITagInfoProvider tagInfoProvider;

    private CleanerTransformations transformations = null;

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
        this.properties.tagInfoProvider = this.tagInfoProvider;
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
        return clean(file, DEFAULT_CHARSET);
    }

    public TagNode clean(URL url, String charset) throws IOException {
        URLConnection urlConnection = url.openConnection();
        if (charset == null) {
            charset = Utils.getCharsetFromContentTypeString( urlConnection.getHeaderField("Content-Type") );
        }
        if (charset == null) {
            charset = Utils.getCharsetFromContent(url);
        }
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        return clean(url.openStream(), charset);
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
        return clean(url, null);
    }

    public TagNode clean(InputStream in, String charset) throws IOException {
        return clean( new InputStreamReader(in, charset) );
    }

    public TagNode clean(InputStream in) throws IOException {
        return clean(in, DEFAULT_CHARSET);
    }

    public TagNode clean(Reader reader) throws IOException {
        return clean(reader, new CleanTimeValues());
    }

    /**
     * Basic version of the cleaning call.
     * @param reader
     * @return An instance of TagNode object which is the root of the XML tree.
     * @throws IOException
     */
    public TagNode clean(Reader reader, final CleanTimeValues cleanTimeValues) throws IOException {
        cleanTimeValues._openTags = new OpenTags();
        cleanTimeValues._headOpened = false;
        cleanTimeValues._bodyOpened = false;
        cleanTimeValues._headTags.clear();
        cleanTimeValues.allTags.clear();
        setPruneTags(properties.pruneTags, cleanTimeValues);

        cleanTimeValues.htmlNode = createTagNode("html", cleanTimeValues);
        cleanTimeValues.bodyNode = createTagNode("body", cleanTimeValues);
        cleanTimeValues.headNode = createTagNode("head", cleanTimeValues);
        cleanTimeValues.rootNode = null;
        cleanTimeValues.htmlNode.addChild(cleanTimeValues.headNode);
        cleanTimeValues.htmlNode.addChild(cleanTimeValues.bodyNode);

        HtmlTokenizer htmlTokenizer = new HtmlTokenizer(reader, properties, transformations, tagInfoProvider) {
            @Override
            void makeTree(List<BaseToken> tokenList) {
                HtmlCleaner.this.makeTree( tokenList, tokenList.listIterator(tokenList.size() - 1), cleanTimeValues );
            }

            @Override
            TagNode createTagNode(String name) {
                return HtmlCleaner.this.createTagNode(name, cleanTimeValues); 
            }
        };

		htmlTokenizer.start();

        List<BaseToken> nodeList = htmlTokenizer.getTokenList();
        closeAll(nodeList, cleanTimeValues);
        createDocumentNodes(nodeList, cleanTimeValues);

        calculateRootNode(cleanTimeValues);

        // if there are some nodes to prune from tree
        if ( cleanTimeValues.pruneNodeSet != null && !cleanTimeValues.pruneNodeSet.isEmpty() ) {
            Iterator iterator = cleanTimeValues.pruneNodeSet.iterator();
            while (iterator.hasNext()) {
                TagNode tagNode = (TagNode) iterator.next();
                TagNode parent = tagNode.getParent();
                if (parent != null) {
                    parent.removeChild(tagNode);
                }
            }
        }

        cleanTimeValues.rootNode.setDocType( htmlTokenizer.getDocType() );

        return cleanTimeValues.rootNode;
    }

    private TagNode createTagNode(String name, CleanTimeValues cleanTimeValues) {
        TagNode node = new TagNode(name);
        if ( cleanTimeValues.pruneTagSet != null && name != null && cleanTimeValues.pruneTagSet.contains(name.toLowerCase()) ) {
            cleanTimeValues.pruneNodeSet.add(node);
        }
        return node;
    }

    private TagNode makeTagNodeCopy(TagNode tagNode, CleanTimeValues cleanTimeValues) {
        TagNode copy = tagNode.makeCopy();
        if ( cleanTimeValues.pruneTagSet != null && cleanTimeValues.pruneTagSet.contains(tagNode.getName()) ) {
            cleanTimeValues.pruneNodeSet.add(copy);
        }
        return copy;
    }

    /**
     * Assigns root node to internal variable.
     * Root node of the result depends on parameter "omitHtmlEnvelope".
     * If it is set, then first child of the body will be root node,
     * or html will be root node otherwise.
     */
    private void calculateRootNode(CleanTimeValues cleanTimeValues) {
        cleanTimeValues.rootNode =  cleanTimeValues.htmlNode;

        if (properties.omitHtmlEnvelope) {
            List bodyChildren = cleanTimeValues.bodyNode.getChildren();
            if (bodyChildren != null) {
                for (Object child: bodyChildren) {
                    // if found child that is tag itself, then return it
                    if (child instanceof TagNode) {
                        cleanTimeValues.rootNode = (TagNode)child;
                        break;
                    }
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
	private void addAttributesToTag(TagNode tag, Map attributes) {
		if (attributes != null) {
			Map tagAttributes = tag.getAttributes();
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry currEntry = (Map.Entry) it.next();
				String attName = (String) currEntry.getKey();
				if ( !tagAttributes.containsKey(attName) ) {
					String attValue = (String) currEntry.getValue();
					tag.setAttribute(attName, attValue);
				}
			}
		}
	}

    /**
     * Checks if open fatal tag is missing if there is a fatal tag for
     * the specified tag.
     * @param tag
     */
    private boolean isFatalTagSatisfied(TagInfo tag, CleanTimeValues cleanTimeValues) {
    	if (tag != null) {
            String fatalTagName = tag.getFatalTag();
            return fatalTagName == null ? true : cleanTimeValues._openTags.tagExists(fatalTagName);
    	}

    	return true;
    }

    /**
     * Check if specified tag requires parent tag, but that parent
     * tag is missing in the appropriate context.
     * @param tag
     */
    private boolean mustAddRequiredParent(TagInfo tag, CleanTimeValues cleanTimeValues) {
    	if (tag != null) {
    		String requiredParent = tag.getRequiredParent();
    		if (requiredParent != null) {
	    		String fatalTag = tag.getFatalTag();
                int fatalTagPositon = -1;
                if (fatalTag != null) {
                    TagPos tagPos = cleanTimeValues._openTags.findTag(fatalTag);
                    if (tagPos != null) {
                        fatalTagPositon = tagPos.position;
                    }
                }

	    		// iterates through the list of open tags from the end and check if there is some higher
	    		ListIterator<TagPos> it = cleanTimeValues._openTags.list.listIterator( cleanTimeValues._openTags.list.size() );
	            while ( it.hasPrevious() ) {
	            	TagPos currTagPos = it.previous();
	            	if (tag.isHigher(currTagPos.name)) {
	            		return currTagPos.position <= fatalTagPositon;
	            	}
	            }

	            return true;
    		}
    	}

    	return false;
    }

    private TagNode createTagNode(TagNode startTagToken) {
    	startTagToken.setFormed();
    	return startTagToken;
    }

    private boolean isAllowedInLastOpenTag(BaseToken token, CleanTimeValues cleanTimeValues) {
        TagPos last = cleanTimeValues._openTags.getLastTagPos();
        if (last != null) {
			 if (last.info != null) {
                 return last.info.allowsItem(token);
			 }
		}

		return true;
    }

    private void saveToLastOpenTag(List nodeList, BaseToken tokenToAdd, CleanTimeValues cleanTimeValues) {
        TagPos last = cleanTimeValues._openTags.getLastTagPos();
        if ( last != null && last.info != null && last.info.isIgnorePermitted() ) {
            return;
        }

        TagPos rubbishPos = cleanTimeValues._openTags.findTagToPlaceRubbish();
        if (rubbishPos != null) {
    		TagNode startTagToken = (TagNode) nodeList.get(rubbishPos.position);
            startTagToken.addItemForMoving(tokenToAdd);
        }
    }

    private boolean isStartToken(Object o) {
    	return (o instanceof TagNode) && !((TagNode)o).isFormed();
    }

	void makeTree(List<BaseToken> nodeList, ListIterator<BaseToken> nodeIterator, CleanTimeValues cleanTimeValues) {
		// process while not reach the end of the list
		while ( nodeIterator.hasNext() ) {
			BaseToken token = nodeIterator.next();

            if (token instanceof EndTagToken) {
				EndTagToken endTagToken = (EndTagToken) token;
				String tagName = endTagToken.getName();
				TagInfo tag = tagInfoProvider.getTagInfo(tagName);

				if ( (tag == null && properties.omitUnknownTags) || (tag != null && tag.isDeprecated() && properties.omitDeprecatedTags) ) {
					nodeIterator.set(null);
				} else if ( tag != null && !tag.allowsBody() ) {
					nodeIterator.set(null);
				} else {
					TagPos matchingPosition = cleanTimeValues._openTags.findTag(tagName);

                    if (matchingPosition != null) {
                        List closed = closeSnippet(nodeList, matchingPosition, endTagToken, cleanTimeValues);
                        nodeIterator.set(null);
                        for (int i = closed.size() - 1; i >= 1; i--) {
                            TagNode closedTag = (TagNode) closed.get(i);
                            if ( tag != null && tag.isContinueAfter(closedTag.getName()) ) {
                                nodeIterator.add( makeTagNodeCopy(closedTag, cleanTimeValues) );
                                nodeIterator.previous();
                            }
                        }
                    } else if ( !isAllowedInLastOpenTag(token, cleanTimeValues) ) {
                        saveToLastOpenTag(nodeList, token, cleanTimeValues);
                        nodeIterator.set(null);
                    }
                }
			} else if ( isStartToken(token) ) {
                TagNode startTagToken = (TagNode) token;
				String tagName = startTagToken.getName();
				TagInfo tag = tagInfoProvider.getTagInfo(tagName);

                TagPos lastTagPos = cleanTimeValues._openTags.isEmpty() ? null : cleanTimeValues._openTags.getLastTagPos();
                TagInfo lastTagInfo = lastTagPos == null ? null : tagInfoProvider.getTagInfo(lastTagPos.name);

                // add tag to set of all tags
				cleanTimeValues.allTags.add(tagName);

                // HTML open tag
                if ( "html".equals(tagName) ) {
					addAttributesToTag(cleanTimeValues.htmlNode, startTagToken.getAttributes());
					nodeIterator.set(null);
                // BODY open tag
                } else if ( "body".equals(tagName) ) {
                    cleanTimeValues._bodyOpened = true;
                    addAttributesToTag(cleanTimeValues.bodyNode, startTagToken.getAttributes());
					nodeIterator.set(null);
                // HEAD open tag
                } else if ( "head".equals(tagName) ) {
                    cleanTimeValues._headOpened = true;
                    addAttributesToTag(cleanTimeValues.headNode, startTagToken.getAttributes());
					nodeIterator.set(null);
                // unknown HTML tag and unknown tags are not allowed
                } else if ( (tag == null && properties.omitUnknownTags) || (tag != null && tag.isDeprecated() && properties.omitDeprecatedTags) ) {
                    nodeIterator.set(null);
                // if current tag is unknown, unknown tags are allowed and last open tag doesn't allow any other tags in its body
                } else if ( tag == null && lastTagInfo != null && !lastTagInfo.allowsAnything() ) {
                    saveToLastOpenTag(nodeList, token, cleanTimeValues);
                    nodeIterator.set(null);
                } else if ( tag != null && tag.hasPermittedTags() && cleanTimeValues._openTags.someAlreadyOpen(tag.getPermittedTags()) ) {
                	nodeIterator.set(null);
                // if tag that must be unique, ignore this occurence
                } else if ( tag != null && tag.isUnique() && cleanTimeValues._openTags.tagEncountered(tagName) ) {
                	nodeIterator.set(null);
                // if there is no required outer tag without that this open tag is ignored
                } else if ( !isFatalTagSatisfied(tag, cleanTimeValues) ) {
					nodeIterator.set(null);
                // if there is no required parent tag - it must be added before this open tag
                } else if ( mustAddRequiredParent(tag, cleanTimeValues) ) {
					String requiredParent = tag.getRequiredParent();
					TagNode requiredParentStartToken = createTagNode(requiredParent, cleanTimeValues);
					nodeIterator.previous();
					nodeIterator.add(requiredParentStartToken);
					nodeIterator.previous();
                // if last open tag has lower presidence then this, it must be closed
                } else if ( tag != null && lastTagPos != null && tag.isMustCloseTag(lastTagInfo) ) {
					List closed = closeSnippet(nodeList, lastTagPos, startTagToken, cleanTimeValues);
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
								nodeIterator.add( makeTagNodeCopy(currStartToken, cleanTimeValues) );
							}

                            // back to the previous place, before adding new start tokens
							for (int i = 0; i < toBeCopied.size(); i++) {
								nodeIterator.previous();
							}
                        }
					}

                    nodeIterator.previous();
				// if this open tag is not allowed inside last open tag, then it must be moved to the place where it can be
                } else if ( !isAllowedInLastOpenTag(token, cleanTimeValues) ) {
                    saveToLastOpenTag(nodeList, token, cleanTimeValues);
                    nodeIterator.set(null);
				// if it is known HTML tag but doesn't allow body, it is immediately closed
                } else if ( tag != null && !tag.allowsBody() ) {
					TagNode newTagNode = createTagNode(startTagToken);
                    addPossibleHeadCandidate(tag, newTagNode, cleanTimeValues);
                    nodeIterator.set(newTagNode);
				// default case - just remember this open tag and go further
                } else {
                    cleanTimeValues._openTags.addTag( tagName, nodeIterator.previousIndex() );
                }
			} else {
				if ( !isAllowedInLastOpenTag(token, cleanTimeValues) ) {
                    saveToLastOpenTag(nodeList, token, cleanTimeValues);
                    nodeIterator.set(null);
				}
			}
		}
    }

	private void createDocumentNodes(List listNodes, CleanTimeValues cleanTimeValues) {
		Iterator it = listNodes.iterator();
        while (it.hasNext()) {
            Object child = it.next();

            if (child == null) {
            	continue;
            }

			boolean toAdd = true;

            if (child instanceof TagNode) {
                TagNode node = (TagNode) child;
                TagInfo tag = tagInfoProvider.getTagInfo( node.getName() );
                addPossibleHeadCandidate(tag, node, cleanTimeValues);
			} else {
				if (child instanceof ContentNode) {
					toAdd = !"".equals(child.toString());
				}
			}

			if (toAdd) {
				cleanTimeValues.bodyNode.addChild(child);
			}
        }

        // move all viable head candidates to head section of the tree
        Iterator headIterator = cleanTimeValues._headTags.iterator();
        while (headIterator.hasNext()) {
            TagNode headCandidateNode = (TagNode) headIterator.next();

            // check if this node is already inside a candidate for moving to head
            TagNode parent = headCandidateNode.getParent();
            boolean toMove = true;
            while (parent != null) {
                if ( cleanTimeValues._headTags.contains(parent) ) {
                    toMove = false;
                    break;
                }
                parent = parent.getParent();
            }

            if (toMove) {
                headCandidateNode.removeFromTree();
                cleanTimeValues.headNode.addChild(headCandidateNode);
            }
        }
    }

	private List closeSnippet(List nodeList, TagPos tagPos, Object toNode, CleanTimeValues cleanTimeValues) {
		List closed = new ArrayList();
		ListIterator it = nodeList.listIterator(tagPos.position);

		TagNode tagNode = null;
		Object item = it.next();
		boolean isListEnd = false;

		while ( (toNode == null && !isListEnd) || (toNode != null && item != toNode) ) {
			if ( isStartToken(item) ) {
                TagNode startTagToken = (TagNode) item;
                closed.add(startTagToken);
                List<BaseToken> itemsToMove = startTagToken.getItemsToMove();
                if (itemsToMove != null) {
            		OpenTags prevOpenTags = cleanTimeValues._openTags;
            		cleanTimeValues._openTags = new OpenTags();
            		makeTree(itemsToMove, itemsToMove.listIterator(0), cleanTimeValues);
                    closeAll(itemsToMove, cleanTimeValues);
                    startTagToken.setItemsToMove(null);
                    cleanTimeValues._openTags = prevOpenTags;
                }

                TagNode newTagNode = createTagNode(startTagToken);
                TagInfo tag = tagInfoProvider.getTagInfo( newTagNode.getName() );
                addPossibleHeadCandidate(tag, newTagNode, cleanTimeValues);
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

                cleanTimeValues._openTags.removeTag( newTagNode.getName() );
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
    private void closeAll(List<BaseToken> nodeList, CleanTimeValues cleanTimeValues) {
        TagPos firstTagPos = cleanTimeValues._openTags.findFirstTagPos();
        if (firstTagPos != null) {
            closeSnippet(nodeList, firstTagPos, null, cleanTimeValues);
        }
    }

    /**
     * Checks if specified tag with specified info is candidate for moving to head section.
     * @param tagInfo
     * @param tagNode
     */
    private void addPossibleHeadCandidate(TagInfo tagInfo, TagNode tagNode, CleanTimeValues cleanTimeValues) {
        if (tagInfo != null && tagNode != null) {
            if ( tagInfo.isHeadTag() || (tagInfo.isHeadAndBodyTag() && cleanTimeValues._headOpened && !cleanTimeValues._bodyOpened) ) {
                cleanTimeValues._headTags.add(tagNode);
            }
        }
    }

    public CleanerProperties getProperties() {
        return properties;
    }

    private void setPruneTags(String pruneTags, CleanTimeValues cleanTimeValues) {
        cleanTimeValues.pruneTagSet.clear();
        cleanTimeValues.pruneNodeSet.clear();
        if (pruneTags != null) {
            StringTokenizer tokenizer = new StringTokenizer(pruneTags, ",");
            while ( tokenizer.hasMoreTokens() ) {
                cleanTimeValues.pruneTagSet.add( tokenizer.nextToken().trim().toLowerCase() );
            }
        }
    }

    /**
     * @return ITagInfoProvider instance for this HtmlCleaner
     */
    public ITagInfoProvider getTagInfoProvider() {
        return tagInfoProvider;
    }

    /**
     * @return Transormations defined for this instance of cleaner
     */
    public CleanerTransformations getTransformations() {
        return transformations;
    }

    /**
     * Sets tranformations for this cleaner instance.
     * @param transformations
     */
    public void setTransformations(CleanerTransformations transformations) {
        this.transformations = transformations;
    }

    /**
     * For the specified node, returns it's content as string.
     * @param node
     */
    public String getInnerHtml(TagNode node) {
        if (node != null) {
            try {
                String content = new SimpleXmlSerializer(properties).getAsString(node);
                int index1 = content.indexOf("<" + node.getName());
                index1 = content.indexOf('>', index1 + 1);
                int index2 = content.lastIndexOf('<');
                return index1 >= 0 && index1 <= index2 ? content.substring(index1 + 1, index2) : null;
            } catch (IOException e) {
                throw new HtmlCleanerException(e);
            }
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

}