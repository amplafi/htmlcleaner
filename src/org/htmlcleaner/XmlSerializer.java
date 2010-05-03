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
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Abstract XML serializer - contains common logic for descendants.</p>
 *
 * Created by: Vladimir Nikic<br/>
 * Date: November, 2006.
 */
public abstract class XmlSerializer {

	/**
     * 
     */
    public static final String XMLNS_NAMESPACE = "xmlns";
    /**
     * 
     */
    public static final String SAFE_BEGIN_CDATA = "/*<![CDATA[*/";
    /**
     * 
     */
    public static final String SAFE_END_CDATA = "/*]]>*/";
    protected CleanerProperties props;
	private boolean creatingHtmlDom;

    protected XmlSerializer() {

    }
	protected XmlSerializer(CleanerProperties props) {
		this.props = props;
    }

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

    public void writeXmlToStream(TagNode tagNode, OutputStream out, String charset) throws IOException {
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset));
         writeXml( tagNode, writer, charset );
         writer.flush();
         writer.close();
    }

    public void writeXmlToStream(TagNode tagNode, OutputStream out) throws IOException {
         writeXmlToStream( tagNode, out, props.getCharset() );
    }

    public void writeXmlToFile(TagNode tagNode, String fileName, String charset) throws IOException {
        writeXmlToStream(tagNode, new FileOutputStream(fileName), charset );
    }

    public void writeXmlToFile(TagNode tagNode, String fileName) throws IOException {
        writeXmlToFile(tagNode,fileName, props.getCharset());
    }

    public String getXmlAsString(CleanerProperties cleanerProperties, String htmlContent, String charset) {
        this.props = cleanerProperties;
        HtmlCleaner htmlCleaner = new HtmlCleaner(cleanerProperties);
        TagNode tagNode= htmlCleaner.clean(htmlContent);
        return getXmlAsString(tagNode, charset==null||charset.length()==0?props.getCharset():charset);
    }

    public String getXmlAsString(TagNode tagNode, String charset) {
        StringWriter writer = new StringWriter();
        try {
            writeXml(tagNode, writer, charset);
        } catch (IOException e) {
            throw new HtmlCleanerException(e);
        }
        return writer.getBuffer().toString();
    }

    public String getXmlAsString(TagNode tagNode) {
        return getXmlAsString(tagNode, props.getCharset());
    }

    public void writeXml(TagNode tagNode, Writer writer, String charset) throws IOException {
        if ( !props.isOmitXmlDeclaration() ) {
            String declaration = "<?xml version=\"1.0\"";
            if (charset != null) {
                declaration += " encoding=\"" + charset + "\"";
            }
            declaration += "?>";
            writer.write(declaration + "\n");
		}

		if ( !props.isOmitDoctypeDeclaration() ) {
			DoctypeToken doctypeToken = tagNode.getDocType();
			if ( doctypeToken != null ) {
				doctypeToken.serialize(this, writer);
			}
		}

		serialize(tagNode, writer);
    }

	protected String escapeXml(String xmlContent) {
		return Utils.escapeXml(xmlContent, props, isCreatingHtmlDom());
	}

	/**
	 * encapsulate content with <[CDATA[ ]]> for things like script and style elements
	 * @param tagNode
	 * @return true if <[CDATA[ ]]> should be used.
	 */
	protected boolean dontEscape(TagNode tagNode) {
	    // make sure <script src=..></script> doesn't get turned into <script src=..><[CDATA[]]></script>
	    // TODO check for blank content as well.
		return props.isUseCdataForScriptAndStyle() && isScriptOrStyle(tagNode) && !tagNode.isEmpty();
	}

	protected boolean isScriptOrStyle(TagNode tagNode) {
		String tagName = tagNode.getName();
		return "script".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName);
	}

    protected boolean isMinimizedTagSyntax(TagNode tagNode) {
        final TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
        return tagNode.isEmpty() && (tagInfo == null || tagInfo.isMinimizedTagPermitted()) &&
               ( props.isUseEmptyElementTags() || (tagInfo != null && tagInfo.isEmptyTag()) );
    }

    protected void serializeOpenTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        if ( !isForbiddenTag(tagNode)) {
            String tagName = tagNode.getName();
            Map tagAtttributes = tagNode.getAttributes();
    
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
                writer.write(">"+SAFE_BEGIN_CDATA);
            } else {
            	writer.write(">");
            }
        }
    }
    protected void serializeOpenTag(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, true);
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

    protected void serializeEndTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
    	if ( !isForbiddenTag(tagNode)) {
    	    String tagName = tagNode.getName();
        	if (dontEscape(tagNode)) {
                // because we are not considering if the file is xhtml or html,
                // we need to put a javascript comment in front of the CDATA in case this is NOT xhtml
        		writer.write(SAFE_END_CDATA);
        	}
    
        	writer.write( "</" + tagName + ">" );
    
            if (newLine) {
        		writer.write("\n");
        	}
    	}
    }
    protected void serializeEndTag(TagNode tagNode, Writer writer) throws IOException {
    	serializeEndTag(tagNode, writer, true);
    }


    protected abstract void serialize(TagNode tagNode, Writer writer) throws IOException;

}