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
 * Main HTML tokenizer.
 * <p>It's task is to parse HTML and produce list of valid tokens:
 * open tag tokens, end tag tokens, contents (text) and comments.
 * As soon as new item is added to token list, cleaner is invoked
 * to clean current list at the end.</p>
 */
abstract public class HtmlTokenizer {
	
	private final static int WORKING_BUFFER_SIZE = 1024;

    private BufferedReader _reader;
    private char[] _working = new char[WORKING_BUFFER_SIZE];
    
    private transient int _pos = 0;
    private transient int _len = -1;

    private transient char _saved[] = new char[512];
    private transient int _savedLen = 0;

    private transient DoctypeToken _docType = null;
    private transient TagToken _currentTagToken = null;
    private transient List<BaseToken> _tokenList = new ArrayList<BaseToken>();

    private boolean _asExpected = true;

    private boolean _isScriptContext = false;

    private CleanerProperties props;

    private boolean isOmitUnknownTags;
    private boolean isTreatUnknownTagsAsContent;
    private boolean isOmitDeprecatedTags;
    private boolean isTreatDeprecatedTagsAsContent;
    private boolean isNamespacesAware;
    private boolean isOmitComments;
    private boolean isAllowMultiWordAttributes;
    private boolean isAllowHtmlInsideAttributes;

    private CleanerTransformations transformations;
    private ITagInfoProvider tagInfoProvider;

    private StringBuilder commonStr = new StringBuilder();

    /**
     * Constructor - cretes instance of the parser with specified content.
     * 
     * @param reader
     * @param props
     * @param transformations
     * @param tagInfoProvider
     * 
     * @throws IOException
     */
    public HtmlTokenizer(Reader reader, CleanerProperties props, CleanerTransformations transformations, ITagInfoProvider tagInfoProvider) throws IOException {
        this._reader = new BufferedReader(reader);
        this.props = props;
        this.isOmitUnknownTags = props.isOmitUnknownTags();
        this.isTreatUnknownTagsAsContent = props.isTreatUnknownTagsAsContent();
        this.isOmitDeprecatedTags = props.isOmitDeprecatedTags();
        this.isTreatDeprecatedTagsAsContent = props.isTreatDeprecatedTagsAsContent();
        this.isNamespacesAware = props.isNamespacesAware();
        this.isOmitComments = props.isOmitComments();
        this.isAllowMultiWordAttributes = props.isAllowMultiWordAttributes();
        this.isAllowHtmlInsideAttributes = props.isAllowHtmlInsideAttributes();
        this.transformations = transformations;
        this.tagInfoProvider = tagInfoProvider;
    }

    private void addToken(BaseToken token) {
        _tokenList.add(token);
        makeTree(_tokenList);
    }

    abstract void makeTree(List<BaseToken> tokenList);

    abstract TagNode createTagNode(String name);

    private void readIfNeeded(int neededChars) throws IOException {
        if (_len == -1 && _pos + neededChars >= WORKING_BUFFER_SIZE) {
            int numToCopy = WORKING_BUFFER_SIZE - _pos;
            System.arraycopy(_working, _pos, _working, 0, numToCopy);
    		_pos = 0;

            int expected = WORKING_BUFFER_SIZE - numToCopy;
            int size = 0;
            int charsRead;
            int offset = numToCopy;
            do {
                charsRead = _reader.read(_working, offset, expected);
                if (charsRead >= 0) {
                    size += charsRead;
                    offset += charsRead;
                    expected -= charsRead;
                }
            } while (charsRead >= 0 && expected > 0);

            if (expected > 0) {
    			_len = size + numToCopy;
            }

            // convert invalid XML characters to spaces
            for (int i = 0; i < (_len >= 0 ? _len : WORKING_BUFFER_SIZE); i++) {
                int ch = _working[i];
                if (ch >= 1 && ch <= 32 && ch != 10 && ch != 13) {
                    _working[i] = ' ';
                }
            }
        }
    }

    List<BaseToken> getTokenList() {
    	return this._tokenList;
    }

    private void go() throws IOException {
    	_pos++;
    	readIfNeeded(0);
    }

    private void go(int step) throws IOException {
    	_pos += step;
    	readIfNeeded(step - 1);
    }

    /**
     * Checks if content starts with specified value at the current position.
     * @param value
     * @return true if starts with specified value, false otherwise.
     * @throws IOException
     */
    private boolean startsWith(String value) throws IOException {
        int valueLen = value.length();
        readIfNeeded(valueLen);
        if (_len >= 0 && _pos + valueLen  > _len) {
            return false;
        }

        for (int i = 0; i < valueLen; i++) {
        	char ch1 = Character.toLowerCase( value.charAt(i) );
        	char ch2 = Character.toLowerCase( _working[_pos + i] );
        	if (ch1 != ch2) {
        		return false;
        	}
        }

        return true;
    }

    private boolean startsWithSimple(String value) throws IOException {
        int valueLen = value.length();
        readIfNeeded(valueLen);
        if (_len >= 0 && _pos + valueLen  > _len) {
            return false;
        }

        for (int i = 0; i < valueLen; i++) {
        	if (value.charAt(i) != _working[_pos + i]) {
        		return false;
        	}
        }

        return true;
    }

    /**
     * Checks if character at specified position is whitespace.
     * @param position
     * @return true is whitespace, false otherwise.
     */
    private boolean isWhitespace(int position) {
    	if (_len >= 0 && position >= _len) {
            return false;
        }

        return Character.isWhitespace( _working[position] );
    }

    /**
     * Checks if character at current runtime position is whitespace.
     * @return true is whitespace, false otherwise.
     */
    private boolean isWhitespace() {
        return isWhitespace(_pos);
    }

    private boolean isWhitespaceSafe() {
        return Character.isWhitespace( _working[_pos] );
    }

    /**
     * Checks if character at specified position is equal to specified char.
     * @param position
     * @param ch
     * @return true is equals, false otherwise.
     */
    private boolean isChar(int position, char ch) {
    	if (_len >= 0 && position >= _len) {
            return false;
        }

        return Character.toLowerCase(ch) == Character.toLowerCase(_working[position]);
    }

    /**
     * Checks if character at current runtime position is equal to specified char.
     * @param ch
     * @return true is equal, false otherwise.
     */
    private boolean isChar(char ch) {
        return isChar(_pos, ch);
    }

    private boolean isCharSimple(char ch) {
        return (_len < 0 || _pos < _len) && (ch == _working[_pos]);
    }

    /**
     * @return Current character to be read, but first it must be checked if it exists.
     * This method is made for performance reasons to be used instead of isChar(...).
     */
    private char getCurrentChar() {
        return _working[_pos];
    }

    private boolean isCharEquals(char ch) {
        return _working[_pos] == ch;
    }

    /**
     * Checks if character at specified position can be identifier start.
     * @param position
     * @return true is may be identifier start, false otherwise.
     */
    private boolean isIdentifierStartChar(int position) {
    	if (_len >= 0 && position >= _len) {
            return false;
        }

        char ch = _working[position];
        return Character.isUnicodeIdentifierStart(ch) || ch == '_';
    }

    /**
     * Checks if character at current runtime position can be identifier start.
     * @return true is may be identifier start, false otherwise.
     */
    private boolean isIdentifierStartChar() {
        return isIdentifierStartChar(_pos);
    }

    /**
     * Checks if character at current runtime position can be identifier part.
     * @return true is may be identifier part, false otherwise.
     */
    private boolean isIdentifierChar() {
    	if (_len >= 0 && _pos >= _len) {
            return false;
        }

        char ch = _working[_pos];
        return Character.isUnicodeIdentifierStart(ch) || Character.isDigit(ch) || Utils.isIdentifierHelperChar(ch);
    }

    private boolean isValidXmlChar() {
        return isAllRead() || Utils.isValidXmlChar(_working[_pos]);
    }

    private boolean isValidXmlCharSafe() {
        return Utils.isValidXmlChar(_working[_pos]);
    }

    /**
     * Checks if end of the content is reached.
     */
    private boolean isAllRead() {
        return _len >= 0 && _pos >= _len;
    }

    /**
     * Saves specified character to the temporary buffer.
     * @param ch
     */
    private void save(char ch) {
        if (_savedLen >= _saved.length) {
            char newSaved[] = new char[_saved.length + 512];
            System.arraycopy(_saved, 0, newSaved, 0, _saved.length);
            _saved = newSaved;
        }
        _saved[_savedLen++] = ch;
    }

    /**
     * Saves character at current runtime position to the temporary buffer.
     */
    private void saveCurrent() {
        if (!isAllRead()) {
            save( _working[_pos] );
        }
    }

    private void saveCurrentSafe() {
        save( _working[_pos] );
    }

    /**
     * Saves specified number of characters at current runtime position to the temporary buffer.
     * @throws IOException
     */
    private void saveCurrent(int size) throws IOException {
    	readIfNeeded(size);
        int pos = _pos;
        while ( !isAllRead() && (size > 0) ) {
            save( _working[pos] );
            pos++;
            size--;
        }
    }

    /**
     * Skips whitespaces at current position and moves foreward until
     * non-whitespace character is found or the end of content is reached.
     * @throws IOException
     */
    private void skipWhitespaces() throws IOException {
        while ( !isAllRead() && isWhitespaceSafe() ) {
            saveCurrentSafe();
            go();
        }
    }

    private boolean addSavedAsContent() {
        if (_savedLen > 0) {
            addToken(new ContentNode(_saved, _savedLen));
            _savedLen = 0;
            return true;
        }

        return false;
    }

    /**
     * Starts parsing HTML.
     * @throws IOException
     */
    void start() throws IOException {
    	// initialize runtime values
        _currentTagToken = null;
        _tokenList.clear();
        _asExpected = true;
        _isScriptContext = false;

        boolean isLateForDoctype = false;

        this._pos = WORKING_BUFFER_SIZE;
        readIfNeeded(0);

        boolean isScriptEmpty = true;

        while ( !isAllRead() ) {
            // resets all the runtime values
            _savedLen = 0;
            _currentTagToken = null;
            _asExpected = true;

            // this is enough for making decision
            readIfNeeded(10);

            if (_isScriptContext) {
                if ( startsWith("</script") && (isWhitespace(_pos + 8) || isChar(_pos + 8, '>')) ) {
                    tagEnd();
                } else if ( isScriptEmpty && startsWithSimple("<!--") ) {
                    comment();
                } else {
                    boolean isTokenAdded = content();
                    if (isScriptEmpty && isTokenAdded) {
                        final BaseToken lastToken = _tokenList.get(_tokenList.size() - 1);
                        if (lastToken != null) {
                            final String lastTokenAsString = lastToken.toString();
                            if (lastTokenAsString != null && lastTokenAsString.trim().length() > 0) {
                                isScriptEmpty = false;
                            }
                        }
                    }
                }
                if (!_isScriptContext) {
                    isScriptEmpty = true;
                }
            } else {
                if ( startsWith("<!doctype") ) {
                	if ( !isLateForDoctype ) {
                		doctype();
                		isLateForDoctype = true;
                	} else {
                		ignoreUntil('<');
                	}
                } else if ( startsWithSimple("</") && isIdentifierStartChar(_pos + 2) ) {
                	isLateForDoctype = true;
                    tagEnd();
                } else if ( startsWithSimple("<!--") ) {
                    comment();
                } else if ( startsWithSimple("<") && isIdentifierStartChar(_pos + 1) ) {
                	isLateForDoctype = true;
                    tagStart();
                } else if ( props.isIgnoreQuestAndExclam() && (startsWithSimple("<!") || startsWithSimple("<?")) ) {
                    ignoreUntil('>');
                    if (isCharSimple('>')) {
                        go();
                    }
                } else {
                    content();
                }
            }
        }

        _reader.close();
    }

    /**
     * Checks if specified tag name is one of the reserved tags: HTML, HEAD or BODY
     * @param tagName
     * @return
     */
    private boolean isReservedTag(String tagName) {
        tagName = tagName.toLowerCase();
        return "html".equals(tagName) || "head".equals(tagName) || "body".equals(tagName);
    }

    /**
     * Parses start of the tag.
     * It expects that current position is at the "<" after which
     * the tag's name follows.
     * @throws IOException
     */
    private void tagStart() throws IOException {
        saveCurrent();
        go();

        if ( isAllRead() ) {
            return;
        }

        String tagName = identifier();

        TagTransformation tagTransformation = null;
        if (transformations != null && transformations.hasTransformationForTag(tagName)) {
            tagTransformation = transformations.getTransformation(tagName);
            if (tagTransformation != null) {
                tagName = tagTransformation.getDestTag();
            }
        }

        if (tagName != null) {
            TagInfo tagInfo = tagInfoProvider.getTagInfo(tagName);
            if ( (tagInfo == null && !isOmitUnknownTags && isTreatUnknownTagsAsContent && !isReservedTag(tagName)) ||
                 (tagInfo != null && tagInfo.isDeprecated() && !isOmitDeprecatedTags && isTreatDeprecatedTagsAsContent) ) {
                content();
                return;
            }
        }

        TagNode tagNode = createTagNode(tagName);
        _currentTagToken = tagNode;

        if (_asExpected) {
            skipWhitespaces();
            tagAttributes();

            if (tagName != null) {
                if (tagTransformation != null) {
                    tagNode.transformAttributes(tagTransformation);
                }
                addToken(_currentTagToken);
            }

            if ( isCharSimple('>') ) {
            	go();
                if ( "script".equalsIgnoreCase(tagName) ) {
                    _isScriptContext = true;
                }
            } else if ( startsWithSimple("/>") ) {
            	go(2);
                if ( "script".equalsIgnoreCase(tagName) ) {
                    addToken( new EndTagToken(tagName) );
                }
            }

            _currentTagToken = null;
        } else {
        	addSavedAsContent();
        }
    }


    /**
     * Parses end of the tag.
     * It expects that current position is at the "<" after which
     * "/" and the tag's name follows.
     * @throws IOException
     */
    private void tagEnd() throws IOException {
        saveCurrent(2);
        go(2);

        if ( isAllRead() ) {
            return;
        }

        String tagName = identifier();
        if (transformations != null && transformations.hasTransformationForTag(tagName)) {
            TagTransformation tagTransformation = transformations.getTransformation(tagName);
            if (tagTransformation != null) {
                tagName = tagTransformation.getDestTag();
            }
        }

        if (tagName != null) {
            TagInfo tagInfo = tagInfoProvider.getTagInfo(tagName);
            if ( (tagInfo == null && !isOmitUnknownTags && isTreatUnknownTagsAsContent && !isReservedTag(tagName)) ||
                 (tagInfo != null && tagInfo.isDeprecated() && !isOmitDeprecatedTags && isTreatDeprecatedTagsAsContent) ) {
                content();
                return;
            }
        }

        _currentTagToken = new EndTagToken(tagName);

        if (_asExpected) {
            skipWhitespaces();
            tagAttributes();

            if (tagName != null) {
                addToken(_currentTagToken);
            }

            if ( isCharSimple('>') ) {
            	go();
            }

            if ( "script".equalsIgnoreCase(tagName) ) {
                _isScriptContext = false;
            }

            _currentTagToken = null;
        } else {
            addSavedAsContent();
        }
    }

    /**
     * Parses an identifier from the current position.
     * @throws IOException
     */
    private String identifier() throws IOException {
        _asExpected = true;

        if ( !isIdentifierStartChar() ) {
            _asExpected = false;
            return null;
        }

        commonStr.delete(0, commonStr.length());

        while ( !isAllRead() && isIdentifierChar() ) {
            saveCurrentSafe();
            commonStr.append( _working[_pos] );
            go();
        }

        // strip invalid characters from the end
        while ( commonStr.length() > 0 && Utils.isIdentifierHelperChar(commonStr.charAt(commonStr.length() - 1)) ) {
            commonStr.deleteCharAt( commonStr.length() - 1 );
        }

        if ( commonStr.length() == 0 ) {
            return null;
        }

        String id = commonStr.toString();

        int columnIndex = id.indexOf(':');
        if (columnIndex >= 0) {
            String prefix = id.substring(0, columnIndex);
            String suffix = id.substring(columnIndex + 1);
            int nextColumnIndex = suffix.indexOf(':');
            if (nextColumnIndex >= 0) {
                suffix = suffix.substring(0, nextColumnIndex);
            }
            id = isNamespacesAware ? (prefix + ":" + suffix) : suffix;
        }

        return id;
    }

    /**
     * Parses list tag attributes from the current position.
     * @throws IOException
     */
    private void tagAttributes() throws IOException {
        while( !isAllRead() && _asExpected && !isCharSimple('>') && !startsWithSimple("/>") ) {
            skipWhitespaces();
            String attName = identifier();

            if (!_asExpected) {
                if ( !isCharSimple('<') && !isCharSimple('>') && !startsWithSimple("/>") ) {
                    if (isValidXmlChar()) {
                        saveCurrent();
                    }
                    go();
                }

                if (!isCharSimple('<')) {
                    _asExpected = true;
                }

                continue;
            }

            String attValue;

            skipWhitespaces();
            if ( isCharSimple('=') ) {
                saveCurrentSafe();
                go();
                attValue = attributeValue();
            } else if (CleanerProperties.BOOL_ATT_EMPTY.equals(props.booleanAttributeValues)) {
                attValue = "";
            } else if (CleanerProperties.BOOL_ATT_TRUE.equals(props.booleanAttributeValues)) {
                attValue = "true";
            } else {
                attValue = attName;
            }

            if (_asExpected) {
                _currentTagToken.setAttribute(attName, attValue);
            }
        }
    }

    /**
     * Parses a single tag attribute - it is expected to be in one of the forms:
     * 		name=value
     * 		name="value"
     * 		name='value'
     * 		name
     * @throws IOException
     */
    private String attributeValue() throws IOException {
        skipWhitespaces();
        
        if ( isCharSimple('<') || isCharSimple('>') || startsWithSimple("/>") ) {
        	return "";
        }

        boolean isQuoteMode = false;
        boolean isAposMode = false;

        commonStr.delete(0, commonStr.length());

        if ( isCharSimple('\'') ) {
            isAposMode = true;
            saveCurrentSafe();
            go();
        } else if ( isCharSimple('\"') ) {
            isQuoteMode = true;
            saveCurrentSafe();
            go();
        }

        while ( !isAllRead() &&
                ( ((isAposMode && !isCharEquals('\'') || isQuoteMode && !isCharEquals('\"')) && (isAllowHtmlInsideAttributes || !isCharEquals('>') && !isCharEquals('<')) && (isAllowMultiWordAttributes || !isWhitespaceSafe())) ||
                  (!isAposMode && !isQuoteMode && !isWhitespaceSafe() && !isCharEquals('>') && !isCharEquals('<'))
                )
              ) {
            if (isValidXmlCharSafe()) {
                commonStr.append( _working[_pos] );
                saveCurrentSafe();
            }
            go();
        }

        if ( isCharSimple('\'') && isAposMode ) {
            saveCurrentSafe();
            go();
        } else if ( isCharSimple('\"') && isQuoteMode ) {
            saveCurrentSafe();
            go();
        }


        return commonStr.toString();
    }

    private boolean content() throws IOException {
        while ( !isAllRead() ) {
            if (isValidXmlCharSafe()) {
                saveCurrentSafe();
            }
            go();

            if ( isCharSimple('<') ) {
                break;
            }
        }

        return addSavedAsContent();
    }

    private void ignoreUntil(char ch) throws IOException {
        while ( !isAllRead() ) {
        	go();
            if ( isChar(ch) ) {
                break;
            }
        }
    }

    private void comment() throws IOException {
    	go(4);
        while ( !isAllRead() && !startsWithSimple("-->") ) {
            if (isValidXmlCharSafe()) {
                saveCurrentSafe();
            }
            go();
        }

        if (startsWithSimple("-->")) {
        	go(3);
        }

        if (_savedLen > 0) {
            if (!isOmitComments) {
                String hyphenRepl = props.getHyphenReplacementInComment();
                String comment = new String(_saved, 0, _savedLen).replaceAll("--", hyphenRepl + hyphenRepl);

        		if ( comment.length() > 0 && comment.charAt(0) == '-' ) {
        			comment = hyphenRepl + comment.substring(1);
        		}
        		int len = comment.length();
        		if ( len > 0 && comment.charAt(len - 1) == '-' ) {
        			comment = comment.substring(0, len - 1) + hyphenRepl;
        		}

        		addToken( new CommentNode(comment) );
        	}
            _savedLen = 0;
        }
    }
    
    private void doctype() throws IOException {
    	go(9);

    	skipWhitespaces();
    	String part1 = identifier();
	    skipWhitespaces();
	    String part2 = identifier();
	    skipWhitespaces();
	    String part3 = attributeValue();
	    skipWhitespaces();
	    String part4 = attributeValue();
	    
	    ignoreUntil('<');
	    
	    _docType = new DoctypeToken(part1, part2, part3, part4);
    }

    public DoctypeToken getDocType() {
        return _docType;
    }
    
}