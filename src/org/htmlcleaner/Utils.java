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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Common utilities.</p>
 *
 * Created by: Vladimir Nikic<br/>
 * Date: November, 2006.
 */
public class Utils {

    public static String VAR_START = "${";
    public static String VAR_END = "}";
    
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
    				if ( (advanced || recognizeUnicodeChars) && (i < len-1) && (s.charAt(i+1) == '#') ) {
    					int charIndex = i + 2;
    					String unicode = "";
    					while ( charIndex < len &&
                                (isHexadecimalDigit(s.charAt(charIndex)) || s.charAt(charIndex) == 'x' || s.charAt(charIndex) == 'X') 
                              ) {
    						unicode += s.charAt(charIndex);
    						charIndex++;
    					}
    					if (charIndex == len || !"".equals(unicode)) {
    						try {
    							char unicodeChar = unicode.toLowerCase().startsWith("x") ?
                                                        (char)Integer.parseInt(unicode.substring(1), 16) :                                
                                                        (char)Integer.parseInt(unicode);
    							if ( "&<>\'\"".indexOf(unicodeChar) < 0 ) {
	    							int replaceChunkSize = (charIndex < len && s.charAt(charIndex) == ';') ? unicode.length()+1 : unicode.length();
	    							result.append( recognizeUnicodeChars ? String.valueOf(unicodeChar) : "&#" + unicode + ";" );
	    							i += replaceChunkSize + 1;
    							} else {
        							i = charIndex;
        							result.append("&amp;#" + unicode + ";");
    							}
    						} catch (NumberFormatException e) {
    							i = charIndex;
    							result.append("&amp;#" + unicode + ";");
    						}
    					} else {
    						result.append("&amp;");
    					}
    				} else {
    					if (translateSpecialEntities) {
    						// get following sequence of most 10 characters
    						String seq = s.substring(i, i+Math.min(10, len-i));
    						int semiIndex = seq.indexOf(';');
    						if (semiIndex > 0) {
    							String entity = seq.substring(1, semiIndex);
    							Integer code = (Integer) SpecialEntities.entities.get(entity);
    							if (code != null) {
    								int entityLen = entity.length();
                                    if (recognizeUnicodeChars) {
                                        result.append( (char)code.intValue() );
                                    } else {
                                        result.append( "&#" + code + ";" );
                                    }
    								i += entityLen + 1;
    								continue;
    							}
    						}
    					}
    					
    					if (advanced) {
                            String sub = s.substring(i);
                            if ( sub.startsWith("&amp;") ) {
                                result.append(isDomCreation ? "&" : "&amp;");
                                i += 4;
                            } else if ( sub.startsWith("&apos;") ) {
                                result.append(isDomCreation ? "'" : "&apos;");
                                i += 5;
                            } else if ( sub.startsWith("&gt;") ) {
                                result.append(isDomCreation ? ">" : "&gt;");
                                i += 3;
                            } else if ( sub.startsWith("&lt;") ) {
                                result.append(isDomCreation ? "<" : "&lt;");
                                i += 3;
                            } else if ( sub.startsWith("&quot;") ) {
                                result.append(isDomCreation ? "\"" : "&quot;");
                                i += 5;
                            } else {
                                result.append(isDomCreation ? "&" : "&amp;");
                            }
    						
    						continue;
    					}
    					
    					result.append("&amp;");
    				}
    			} else if (ch == '\'') {
    				result.append("&apos;");
    			} else if (ch == '>') {
    				result.append("&gt;");
    			} else if (ch == '<') {
    				result.append("&lt;");
    			} else if (ch == '\"') {
    				result.append("&quot;");
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
    
}