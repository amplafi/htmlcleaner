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

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * <p>Pretty XML serializer - creates resulting XML with indenting lines.</p>
 *
 * Created by: Vladimir Nikic<br/>
 * Date: November, 2006.
 */
public class PrettyXmlSerializer extends XmlSerializer {

	private static final String INDENTATION_STRING = "\t";

	public PrettyXmlSerializer(CleanerProperties props) {
		super(props);
	}

	protected void serialize(TagNode tagNode, Writer writer) throws IOException {
		serializePrettyXml(tagNode, writer, 0);
	}

	/**
	 * @param level
	 * @return Appropriate indentation for the specified depth.
	 */
    private String indent(int level) {
        String result = "";
        while (level > 0) {
            result += INDENTATION_STRING;
            level--;
        }

        return result;
    }

    private String getIndentedText(String content,  int level) {
        String indent = indent(level);
        StringBuffer result = new StringBuffer( content.length() );
        StringTokenizer tokenizer = new StringTokenizer(content, "\n\r");

        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().trim();
            if (!"".equals(line)) {
                result.append(indent + line + "\n");
            }
        }

        return result.toString();
    }

    private String getSingleLineOfChildren(List children) {
        StringBuffer result = new StringBuffer();
        Iterator childrenIt = children.iterator();
        boolean isFirst = true;

        while (childrenIt.hasNext()) {
            Object child = childrenIt.next();

            if ( !(child instanceof ContentToken) ) {
                return null;
            } else {
                ContentToken contentToken = (ContentToken) child;
                String content = contentToken.getContent();

                // if first item trims it from left
                if (isFirst) {
                	content = Utils.ltrim(content);
                }

                // if last item trims it from right
                if (!childrenIt.hasNext()) {
                	content = Utils.rtrim(content);
                }

                if ( content.indexOf("\n") >= 0 || content.indexOf("\r") >= 0 ) {
                    return null;
                }
                result.append(content);
            }

            isFirst = false;
        }

        return result.toString();
    }

    protected void serializePrettyXml(TagNode tagNode, Writer writer, int level) throws IOException {
        List tagChildren = tagNode.getChildren();
        String indent = indent(level);

        writer.write(indent);
        serializeOpenTag(tagNode, writer);

        if ( !isMinimizedTagSyntax(tagNode) ) {
            String singleLine = getSingleLineOfChildren(tagChildren);
            boolean dontEscape = dontEscape(tagNode);
            if (singleLine != null) {
            	if ( !dontEscape(tagNode) ) {
            		writer.write( escapeXml(singleLine) );
            	} else {
            		writer.write( singleLine.replaceAll("]]>", "]]&gt;") );
            	}
            } else {
            	writer.write("\n");
                Iterator childrenIt = tagChildren.iterator();
                while (childrenIt.hasNext()) {
                    Object child = childrenIt.next();
                    if (child instanceof TagNode) {
                        serializePrettyXml( (TagNode)child, writer, level + 1 );
                    } else if (child instanceof ContentToken) {
                        ContentToken contentToken = (ContentToken) child;
                        String content = dontEscape ? contentToken.getContent().replaceAll("]]>", "]]&gt;") : escapeXml(contentToken.getContent());
                        writer.write( getIndentedText(content, level + 1) );
                    } else if (child instanceof CommentToken) {
                        CommentToken commentToken = (CommentToken) child;
                        String content = commentToken.getCommentedContent();
                        writer.write( getIndentedText(content, level + 1) );
                    }
                }
            }

            if (singleLine == null) {
            	writer.write(indent);
            }

            serializeEndTag(tagNode, writer);
        }
    }

}