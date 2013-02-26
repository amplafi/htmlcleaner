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
 * <p>Abstract XML serializer - contains common logic for descendants.</p>
 */
public abstract class XmlSerializer extends Serializer {

    public static final String XMLNS_NAMESPACE = "xmlns";
    public static final String BEGIN_CDATA = "<![CDATA[";
    public static final String END_CDATA = "]]>";
    public static final String SAFE_BEGIN_CDATA = "/*" + BEGIN_CDATA + "*/";
    public static final String SAFE_END_CDATA = "/*" + END_CDATA + "*/";

	protected XmlSerializer(CleanerProperties props) {
		super(props);
    }
	
	private boolean creatingHtmlDom;
	
	 /**
     * @param creatingHtmlDom the creatingHtmlDom to set
     */
    public void setCreatingHtmlDom(boolean creatingHtmlDom) {
        this.creatingHtmlDom = creatingHtmlDom;
    }

    /**
     * @return the creatingHtmlDom
     */
    public boolean isCreatingHtmlDom() {
        return creatingHtmlDom;
    }

    /**
     * @deprecated Use writeToStream() instead.
     */
    @Deprecated
    public void writeXmlToStream(TagNode tagNode, OutputStream out, String charset) throws IOException {
         super.writeToStream(tagNode, out, charset);
    }

    /**
     * @deprecated Use writeToStream() instead.
     */
    @Deprecated
    public void writeXmlToStream(TagNode tagNode, OutputStream out) throws IOException {
         super.writeToStream(tagNode, out);
    }

    /**
     * @deprecated Use writeToFile() instead.
     */
    @Deprecated
    public void writeXmlToFile(TagNode tagNode, String fileName, String charset) throws IOException {
        super.writeToFile(tagNode, fileName, charset);
    }

    /**
     * @deprecated Use writeToFile() instead.
     */
    @Deprecated
    public void writeXmlToFile(TagNode tagNode, String fileName) throws IOException {
        super.writeToFile(tagNode, fileName);
    }

    /**
     * @deprecated Use getAsString() instead.
     */
    @Deprecated
    public String getXmlAsString(TagNode tagNode, String charset) {
        return super.getAsString(tagNode, charset);
    }

    /**
     * @deprecated Use getAsString() instead.
     */
    @Deprecated
    public String getXmlAsString(TagNode tagNode) {
        return super.getAsString(tagNode);
    }

    /**
     * @deprecated Use write() instead.
     */
    @Deprecated
    public void writeXml(TagNode tagNode, Writer writer, String charset) throws IOException {
        super.write(tagNode, writer, charset);
    }

    protected String escapeXml(String xmlContent) {
        return Utils.escapeXml(xmlContent, props, isCreatingHtmlDom());
    }

    protected boolean dontEscape(TagNode tagNode) {
        return props.isUseCdataForScriptAndStyle() && isScriptOrStyle(tagNode);
    }

    protected boolean isMinimizedTagSyntax(TagNode tagNode) {
        final TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
        return tagNode.isEmpty() && (tagInfo == null || tagInfo.isMinimizedTagPermitted()) &&
               ( props.isUseEmptyElementTags() || (tagInfo != null && tagInfo.isEmptyTag()) );
    }
    protected void serializeOpenTag(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, true);
    }

    protected void serializeOpenTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        if ( !isForbiddenTag(tagNode)) {
            String tagName = tagNode.getName();
            Map tagAtttributes = tagNode.getAttributes();

            // always have head and body in newline
            if (props.isAddNewlineToHeadAndBody() && isHeadOrBody(tagName)) {
                writer.write("\n");
            }

            writer.write("<" + tagName);
            Iterator it = tagAtttributes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String attName = (String) entry.getKey();
                String attValue = (String) entry.getValue();
                serializeAttribute(tagNode, writer, attName, attValue);
            }

            if ( isMinimizedTagSyntax(tagNode) ) {
                writer.write(" />");
                if (newLine) {
                    writer.write("\n");
                }
            } else if (dontEscape(tagNode)) {
                // because we are not considering if the file is xhtml or html,
                // we need to put a javascript comment in front of the CDATA in case this is NOT xhtml
                writer.write(">");
                if (!tagNode.getText().toString().startsWith(SAFE_BEGIN_CDATA)) {
                    writer.write(SAFE_BEGIN_CDATA);
                }
            } else {
                writer.write(">");
            }
        }
    }
    
    /**
     * @param tagNode
     * @return
     */
    protected boolean isForbiddenTag(TagNode tagNode) {
        // null tagName when rootNode is a dummy node.
        // this happens when omitting the html envelope elements ( <html>, <head>, <body> elements )
        String tagName = tagNode.getName();
        return tagName == null;
    }
    
    protected boolean isHeadOrBody(String tagName) {
        return "head".equalsIgnoreCase(tagName) || "body".equalsIgnoreCase(tagName);
    }
    
    /**
     * This allows overriding to eliminate forbidden attributes (for example javascript attributes onclick, onblur, etc. )
     * @param writer
     * @param attName
     * @param attValue
     * @throws IOException
     */
    protected void serializeAttribute(TagNode tagNode, Writer writer, String attName, String attValue) throws IOException {
        if (!isForbiddenAttribute(tagNode, attName, attValue)) {
            writer.write(" " + attName + "=\"" + escapeXml(attValue) + "\"");
        }
    }
    
    /**
     * Override to add additional conditions.
     * @param tagNode
     * @param attName
     * @param value
     * @return true if the attribute should not be outputed.
     */
    protected boolean isForbiddenAttribute(TagNode tagNode, String attName, String value) {
        return !props.isNamespacesAware() && (XMLNS_NAMESPACE.equals(attName) || attName.startsWith(XMLNS_NAMESPACE +":"));
    }

    protected void serializeEndTag(TagNode tagNode, Writer writer) throws IOException {
       serializeEndTag(tagNode, writer, true);
    }

    protected void serializeEndTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        if ( !isForbiddenTag(tagNode)) {
            String tagName = tagNode.getName();
            if (dontEscape(tagNode)) {
                // because we are not considering if the file is xhtml or html,
                // we need to put a javascript comment in front of the CDATA in case this is NOT xhtml

                if (!tagNode.getText().toString().trim().endsWith(SAFE_END_CDATA)) {
                    writer.write(SAFE_END_CDATA);
                }
            }

            writer.write( "</" + tagName + ">" );

            if (newLine) {
                writer.write("\n");
            }
        }
    }

}