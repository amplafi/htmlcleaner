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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Common utilities.</p>
 */
public class Utils {

    public static String VAR_START = "${";
    public static String VAR_END = "}";

    public static final Map<Character, String> RESERVED_XML_CHARS = new HashMap<Character, String>();

    static {
        RESERVED_XML_CHARS.put('&', "&amp;");
        RESERVED_XML_CHARS.put('<', "&lt;");
        RESERVED_XML_CHARS.put('>', "&gt;");
        RESERVED_XML_CHARS.put('\"', "&quot;");
        RESERVED_XML_CHARS.put('\'', "&apos;");
    }
    
    /**
     * Trims specified string from left.
     * @param s
     */
    public static String ltrim(String s) {
        if (s == null) {
            return null;
        }

        int index = 0;
        int len = s.length();

        while ( index < len && Character.isWhitespace(s.charAt(index)) ) {
            index++;
        }

        return (index >= len) ? "" : s.substring(index);
    }

    /**
     * Trims specified string from right.
     * @param s
     */
    public static String rtrim(String s) {
        if (s == null) {
            return null;
        }

        int len = s.length();
        int index = len;

        while ( index > 0 && Character.isWhitespace(s.charAt(index-1)) ) {
            index--;
        }

        return (index <= 0) ? "" : s.substring(0, index);
    }

    public static String getCharsetFromContentTypeString(String contentType) {
        if (contentType != null) {
            String pattern = "charset=([a-z\\d\\-]*)";
            Matcher matcher = Pattern.compile(pattern,  Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (matcher.find()) {
                String charset = matcher.group(1);
                if (Charset.isSupported(charset)) {
                    return charset;
                }
            }
        }
        
        return null;
    }

    public static String getCharsetFromContent(URL url) throws IOException {
        InputStream stream = url.openStream();
        byte chunk[] = new byte[2048];
        int bytesRead = stream.read(chunk);
        if (bytesRead > 0) {
            String startContent = new String(chunk);
            String pattern = "\\<meta\\s*http-equiv=[\\\"\\']content-type[\\\"\\']\\s*content\\s*=\\s*[\"']text/html\\s*;\\s*charset=([a-z\\d\\-]*)[\\\"\\'\\>]";
            Matcher matcher = Pattern.compile(pattern,  Pattern.CASE_INSENSITIVE).matcher(startContent);
            if (matcher.find()) {
                String charset = matcher.group(1);
                if (Charset.isSupported(charset)) {
                    return charset;
                }
            }
        }

        return null;
    }

    public static boolean isHexadecimalDigit(char ch) {
        return Character.isDigit(ch) ||
               ch == 'A' || ch == 'a' || ch == 'B' || ch == 'b' || ch == 'C' || ch == 'c' ||
               ch == 'D' || ch == 'd' || ch == 'E' || ch == 'e' || ch == 'F' || ch == 'f';
    }

    public static boolean isValidXmlChar(char ch) {
        return ((ch >= 0x20) && (ch <= 0xD7FF)) ||
               (ch == 0x9) ||
               (ch == 0xA) ||
               (ch == 0xD) ||
               ((ch >= 0xE000) && (ch <= 0xFFFD)) ||
               ((ch >= 0x10000) && (ch <= 0x10FFFF));
    }

    public static boolean isReservedXmlChar(char ch) {
        return RESERVED_XML_CHARS.containsKey(ch);
    }

    public static boolean isValidInt(String s, int radix) {
        try {
            Integer.parseInt(s, radix);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Escapes XML string.
     * @param s String to be escaped
     * @param props Cleaner properties gover affect escaping behaviour
     * @param isDomCreation Tells if escaped content will be part of the DOM
     */
    public static String escapeXml(String s, CleanerProperties props, boolean isDomCreation) {
        boolean advanced = props.isAdvancedXmlEscape();
        boolean recognizeUnicodeChars = props.isRecognizeUnicodeChars();
        boolean translateSpecialEntities = props.isTranslateSpecialEntities();

        if (s != null) {
    		int len = s.length();
    		StringBuilder result = new StringBuilder(len);
    		
    		for (int i = 0; i < len; i++) {
    			char ch = s.charAt(i);
    			
    			if (ch == '&') {
    				if ( (advanced || recognizeUnicodeChars) && (i < len-2) && (s.charAt(i+1) == '#') ) {
                        boolean isHex = Character.toLowerCase(s.charAt(i+2)) == 'x';
                        int charIndex = i + (isHex ? 3 : 2);
                        int radix = isHex ? 16 : 10;
                        String unicode = "";
                        while (charIndex < len) {
                            char currCh = s.charAt(charIndex);
                            if (currCh == ';') {
                                break;
                            } else if (isValidInt(unicode + currCh, radix)) {
                                unicode += currCh;
                                charIndex++;
                            } else {
                                charIndex--;
                                break;
                            }
                        }

    					if (isValidInt(unicode, radix)) {
                            char unicodeChar = (char)Integer.parseInt(unicode, radix);
                            if ( !isValidXmlChar(unicodeChar) ) {
                                i = charIndex;
                            } else if ( !isReservedXmlChar(unicodeChar) ) {
                                result.append( recognizeUnicodeChars ? String.valueOf(unicodeChar) : "&#" + unicode + ";" );
                                i = charIndex;
                            } else {
                                i = charIndex;
                                result.append("&#" + unicode + ";");
                            }
    					} else {
    						result.append("&amp;");
    					}
    				} else {
    					if (translateSpecialEntities) {
                            // get minimal following sequence required to recognize some special entitiy
                            String seq = s.substring(i, i + Math.min(SpecialEntity.getMaxEntityLength() + 2, len - i));
    						int semiIndex = seq.indexOf(';');
    						if (semiIndex > 0) {
    							String entityKey = seq.substring(1, semiIndex);
    							SpecialEntity entity = SpecialEntity.getEntity(entityKey);
    							if (entity != null) {
                                    result.append(props.isTransSpecialEntitiesToNCR() ? entity.getDecimalNCR() : entity.getCharacter());
    								i += entityKey.length() + 1;
    								continue;
    							}
    						}
    					}
    					
    					if (advanced) {
                            String sub = s.substring(i);
                            boolean isReservedSeq = false;
                            for (Map.Entry<Character, String> entry: RESERVED_XML_CHARS.entrySet()) {
                                String seq = entry.getValue();
                                if ( sub.startsWith(seq) ) {
                                    result.append( isDomCreation ? entry.getKey() : (props.transResCharsToNCR ? "&#" + (int)entry.getKey() + ";" : seq) );
                                    i += seq.length() - 1;
                                    isReservedSeq = true;
                                    break;
                                }
                            }
                            if (!isReservedSeq) {
                                result.append( isDomCreation ? "&" : (props.transResCharsToNCR ? "&#" + (int)'&' + ";" :  RESERVED_XML_CHARS.get('&')) );
                            }
    						continue;
    					}
    					
    					result.append("&amp;");
    				}
    			} else if (isReservedXmlChar(ch)) {
    				result.append( props.transResCharsToNCR ? "&#" + (int)ch + ";" : (isDomCreation ? ch : RESERVED_XML_CHARS.get(ch)) );
    			} else {
    				result.append(ch);
    			}
    		}
    		
    		return result.toString();
    	}
    	
    	return null;
    }

    /**
     * Checks whether specified object's string representation is empty string (containing of only whitespaces).
     * @param object Object whose string representation is checked
     * @return true, if empty string, false otherwise
     */
    public static boolean isWhitespaceString(Object object) {
        if (object != null) {
            String s = object.toString();
            return s != null && "".equals(s.trim());
        }
        return false;
    }

    /**
     * Checks if specified character can be part of xml identifier (tag name of attribute name)
     * and is not standard identifier character.
     * @param ch Character to be checked
     * @return True if it can be part of xml identifier
     */
    public static boolean isIdentifierHelperChar(char ch) {
        return ':' == ch || '.' == ch || '-' == ch || '_' == ch;
    }

    /**
     * Chacks whether specified string can be valid tag name or attribute name in xml.
     * @param s String to be checked
     * @return True if string is valid xml identifier, false otherwise
     */
    public static boolean isValidXmlIdentifier(String s) {
        if (s != null) {
            int len = s.length();
            if (len == 0) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                if ( (i == 0 && !Character.isUnicodeIdentifierStart(ch) && ch != '_') ||
                     (!Character.isUnicodeIdentifierStart(ch) && !Character.isDigit(ch) && !Utils.isIdentifierHelperChar(ch)) ) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * @param o
     * @return True if specified string is null of contains only whitespace characters
     */
    public static boolean isEmptyString(Object o) {
        return o == null || "".equals(o.toString().trim());
    }

    /**
     * Evaluates string template for specified map of variables. Template string can contain
     * dynamic parts in the form of ${VARNAME}. Each such part is replaced with value of the
     * variable if such exists in the map, or with empty string otherwise.
     * 
     * @param template Template string
     * @param variables Map of variables (can be null)
     * @return Evaluated string
     */
    public static String evaluateTemplate(String template, Map variables) {
        if (template == null) {
            return template;
        }

        StringBuilder result = new StringBuilder();

        int startIndex = template.indexOf(VAR_START);
        int endIndex = -1;

        while (startIndex >= 0 && startIndex < template.length()) {
        	result.append( template.substring(endIndex + 1, startIndex) );
        	endIndex = template.indexOf(VAR_END, startIndex);

        	if (endIndex > startIndex) {
        		String varName = template.substring(startIndex + VAR_START.length(), endIndex);
                Object resultObj = variables != null ? variables.get(varName.toLowerCase()) : "";
                result.append( resultObj == null ? "" : resultObj.toString() );
        	}

        	startIndex = template.indexOf( VAR_START, Math.max(endIndex + VAR_END.length(), startIndex + 1) );
        }

        result.append( template.substring(endIndex + 1) );

        return result.toString();
    }

    public static String[] tokenize(String s, String delimiters) {
        if (s == null) {
            return new String[] {};
        }

        StringTokenizer tokenizer = new StringTokenizer(s, delimiters);
        String result[] = new String[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            result[index++] = tokenizer.nextToken();
        }

        return result;
    }    

    public static void updateTagTransformations(CleanerTransformations transformations, String key, String value) {
        int index = key.indexOf('.');

        // new tag transformation case (tagname[=destname[,preserveatts]])
        if (index <= 0) {
            String destTag = null;
            boolean preserveSourceAtts = true;
            if (value != null) {
                String[] tokens = tokenize(value, ",;");
                if (tokens.length > 0) {
                    destTag = tokens[0];
                }
                if (tokens.length > 1) {
                    preserveSourceAtts = "true".equalsIgnoreCase(tokens[1]) ||
                                         "yes".equalsIgnoreCase(tokens[1]) ||
                                         "1".equals(tokens[1]);
                }
            }
            TagTransformation newTagTrans = new TagTransformation(key, destTag, preserveSourceAtts);
            transformations.addTransformation(newTagTrans);
        } else {    // attribute transformation description
            String[] parts = tokenize(key, ".");
            String tagName = parts[0];
            TagTransformation trans = transformations.getTransformation(tagName);
            if (trans != null) {
                trans.addAttributeTransformation(parts[1], value);
            }
        }
    }

    /**
     * Checks if specified link is full URL.
     *
     * @param link
     * @return True, if full URl, false otherwise.
     */
    public static boolean isFullUrl(String link) {
        if (link == null) {
            return false;
        }
        link = link.trim().toLowerCase();
        return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("file://");
    }

    /**
     * Calculates full URL for specified page URL and link
     * which could be full, absolute or relative like there can
     * be found in A or IMG tags.
     */
    public static String fullUrl(String pageUrl, String link) {
        if (isFullUrl(link)) {
            return link;
        } else if (link != null && link.startsWith("?")) {
            int qindex = pageUrl.indexOf('?');
            int len = pageUrl.length();
            if (qindex < 0) {
                return pageUrl + link;
            } else if (qindex == len - 1) {
                return pageUrl.substring(0, len - 1) + link;
            } else {
                return pageUrl + "&" + link.substring(1);
            }
        }

        boolean isLinkAbsolute = link.startsWith("/");

        if (!isFullUrl(pageUrl)) {
            pageUrl = "http://" + pageUrl;
        }

        int slashIndex = isLinkAbsolute ? pageUrl.indexOf("/", 8) : pageUrl.lastIndexOf("/");
        if (slashIndex <= 8) {
            pageUrl += "/";
        } else {
            pageUrl = pageUrl.substring(0, slashIndex + 1);
        }

        return isLinkAbsolute ? pageUrl + link.substring(1) : pageUrl + link;
    }

    /**
     * @param name
     * @return For xml element name or attribute name returns prefix (part before :) or null if there is no prefix
     */
    public static String getXmlNSPrefix(String name) {
        int colIndex = name.indexOf(':');
        if (colIndex > 0) {
            return name.substring(0, colIndex);
        }

        return null;
    }

    /**
     * @param name
     * @return For xml element name or attribute name returns name after prefix (part after :)
     */
    public static String getXmlName(String name) {
        int colIndex = name.indexOf(':');
        if (colIndex > 0 && colIndex < name.length() - 1) {
            return name.substring(colIndex + 1);
        }

        return name;
    }

}