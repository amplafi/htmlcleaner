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
import java.util.Map;
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

    /**
     * Reads content from the specified URL with specified charset into string
     * @param url
     * @param charset
     * @throws IOException
     */
    public static StringBuffer readUrl(URL url, String charset) throws IOException {
        StringBuffer buffer = new StringBuffer(1024);

        Object content = url.getContent();
        if (content instanceof InputStream) {
            InputStreamReader reader = new InputStreamReader((InputStream)content, charset);
            char[] charArray = new char[1024];

            int charsRead = 0;
            do {
                charsRead = reader.read(charArray);
                if (charsRead >= 0) {
                    buffer.append(charArray, 0, charsRead);
                }
            } while (charsRead > 0);
        }

        return buffer;
    }

    /**
     * Escapes XML string.
     * @param s String to be escaped
     * @param props Cleaner properties affects escaping behaviour
     * @param isDomCreation Tells if escaped content will be part of the DOM
     */
    public static String escapeXml(String s, CleanerProperties props, boolean isDomCreation) {
        boolean advanced = props.isAdvancedXmlEscape();
        boolean recognizeUnicodeChars = props.isRecognizeUnicodeChars();
        boolean translateSpecialEntities = props.isTranslateSpecialEntities();
        return escapeXml(s, advanced, recognizeUnicodeChars, translateSpecialEntities, isDomCreation);
    }
    /**
     * change notes: 
     * 1) convert ascii characters encoded using &#xx; format to the ascii characters -- may be an attempt to slip in malicious html
     * 2) convert &#xxx; format characters to &quot; style representation if available for the character.
     * 3) convert html special entities to xml &#xxx; when outputing in xml
     * @param s
     * @param advanced
     * @param recognizeUnicodeChars
     * @param translateSpecialEntities
     * @param isDomCreation
     * @return
     */
    public static String escapeXml(String s, boolean advanced, boolean recognizeUnicodeChars, boolean translateSpecialEntities, boolean isDomCreation) {
        if (s != null) {
    		int len = s.length();
    		StringBuffer result = new StringBuffer(len);

    		for (int i = 0; i < len; i++) {
    			char ch = s.charAt(i);

    			SpecialEntity code;
    			if (ch == '&') {
    				if ( (advanced || recognizeUnicodeChars) && (i < len-1) && (s.charAt(i+1) == '#') ) {
    					i = convertToUnicode(s, isDomCreation, recognizeUnicodeChars, result, i+2);
    				} else if ((translateSpecialEntities || advanced) &&
				        (code = SpecialEntities.INSTANCE.getSpecialEntity(s.substring(i, i+Math.min(10, len-i)))) != null) {
			            if (translateSpecialEntities && code.isHtmlSpecialEntity()) {
                            if (recognizeUnicodeChars) {
                                result.append( (char)code.intValue() );
                            } else {
                                result.append( "&#" + code + ";" );
                            }
							i += code.getKey().length() + 1;
						} else if (advanced ) {
					        result.append(code.getEscaped(isDomCreation));
		                    i += code.getKey().length()+1;
			            } else {
			                result.append("&amp;");
			            }
    				} else {
    					result.append("&amp;");
    				}
    			} else if ((code = SpecialEntities.INSTANCE.getSpecialEntityByUnicode(ch)) != null ) {
    				result.append(code.getEscaped(isDomCreation));
    			} else {
    				result.append(ch);
    			}
    		}

    		return result.toString();
    	}

    	return null;
    }

    private static final Pattern ASCII_CHAR = Pattern.compile("\\p{Print}");
    /**
     * @param s
     * @param domCreation 
     * @param recognizeUnicodeChars
     * @param result
     * @param i
     * @return
     */
    public static int convertToUnicode(String s, boolean domCreation, boolean recognizeUnicodeChars, StringBuffer result, int i) {
        StringBuilder unicode = new StringBuilder();
        int charIndex = extractCharCode(s, i, true, unicode);
        if (unicode.length() > 0) {
        	try {
        		char unicodeChar = unicode.substring(0,1).equals("x") ?
                                        (char)Integer.parseInt(unicode.substring(1), 16) :
                                        (char)Integer.parseInt(unicode.toString());
                SpecialEntity specialEntity = SpecialEntities.INSTANCE.getSpecialEntityByUnicode(unicodeChar);
                if ( unicodeChar ==0) {
                    // null character &#0Peanut for example
                    // just consume character &
                    result.append("&amp;");
                } else if ( specialEntity != null && 
                        // special characters that are always escaped. 
                        (!specialEntity.isHtmlSpecialEntity() 
                                // OR we are not outputting unicode characters as the characters ( they are staying escaped ) 
                                || !recognizeUnicodeChars)) {
                    result.append(domCreation?specialEntity.getHtmlString():specialEntity.getEscapedXmlString());
                } else if ( recognizeUnicodeChars ) {
                    // output unicode characters as their actual byte code with the exception of characters that have special xml meaning.
                    result.append( String.valueOf(unicodeChar));
                } else if ( ASCII_CHAR.matcher(new String(new char[] { unicodeChar } )).find()) {
                    // ascii printable character. this fancy escaping might be an attempt to slip in dangerous characters (i.e. spelling out <script> ) 
                    // by converting to printable characters we can more easily detect such attacks.
                    result.append(String.valueOf(unicodeChar));
                } else {
        			result.append( "&#").append(unicode).append(";" );
        		}
        	} catch (NumberFormatException e) {
        	    // should never happen now
        		result.append("&amp;#").append(unicode).append(";" );
        	}
        } else {
        	result.append("&amp;");
        }
        return charIndex;
    }

    // TODO have pattern consume leading 0's and discard.
    public static Pattern HEX_STRICT = Pattern.compile("^([x|X][\\p{XDigit}]+)(;?)");
    public static Pattern HEX_RELAXED = Pattern.compile("^0*([x|X][\\p{XDigit}]+)(;?)");
    public static Pattern DECIMAL = Pattern.compile("^([\\p{Digit}]+)(;?)");
    /**
     * <ul>
     * <li>(earlier code was failing on this) - &#138A; is converted by FF to 3 characters: &#138; + 'A' + ';'</li>
     * <li>&#0x138A; is converted by FF to 6? 7? characters: &#0 'x'+'1'+'3'+ '8' + 'A' + ';'
     * #0 is displayed kind of weird</li>
     * <li>&#x138A; is a single character</li>
     * </ul>
     *
     * @param s
     * @param charIndex
     * @param relaxedUnicode '&#0x138;' is treated like '&#x138;'
     * @param unicode
     * @return the index to continue scanning the source string -1 so normal loop incrementing skips the ';'
     */
    public static int extractCharCode(String s, int charIndex, boolean relaxedUnicode, StringBuilder unicode) {
        int len = s.length();
        CharSequence subSequence = s.subSequence(charIndex, Math.min(len,charIndex+15));
        Matcher matcher;
        if( relaxedUnicode ) {
            matcher = HEX_RELAXED.matcher(subSequence);
        } else {
            matcher = HEX_STRICT.matcher(subSequence);
        }
        // silly note: remember calling find() twice finds second match :-)
        if (matcher.find() || ((matcher = DECIMAL.matcher(subSequence)).find())) {
            // -1 so normal loop incrementing skips the ';'
            charIndex += matcher.end() -1;
            unicode.append(matcher.group(1));
        }
        return charIndex;
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
                if ( (i == 0 && !Character.isUnicodeIdentifierStart(ch)) ||
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

        StringBuffer result = new StringBuffer();

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