package org.htmlcleaner;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>DOM serializer - creates xml DOM.</p>
 */
public class DomSerializer {

    protected CleanerProperties props;
    protected boolean escapeXml = true;

    public DomSerializer(CleanerProperties props, boolean escapeXml) {
        this.props = props;
        this.escapeXml = escapeXml;
    }

    public DomSerializer(CleanerProperties props) {
        this(props, true);
    }

    public Document createDOM(TagNode rootNode) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        
        
        Document document;
        
        //
        // Where a DOCTYPE is supplied in the input, ensure that this is in the output DOM. See issue #27
        //
        if (rootNode.getDocType() != null){
        	String qualifiedName = rootNode.getDocType().getPart1();
        	String publicId = rootNode.getDocType().getPublicId();
        	String systemId = rootNode.getDocType().getSystemId();
            DocumentType documentType = impl.createDocumentType(qualifiedName, publicId, systemId);
            document = impl.createDocument(rootNode.getNamespaceURIOnPath(""), qualifiedName, documentType);
        } else {
        	document = builder.newDocument();
        	Element rootElement = document.createElement(rootNode.getName());
        	document.appendChild(rootElement);
        }

        createSubnodes(document, (Element)document.getDocumentElement(), rootNode.getAllChildren());

        return document;
    }

    protected boolean isScriptOrStyle(Element element) {
        String tagName = element.getNodeName();
        return "script".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName);
    }
    /**
     * encapsulate content with <[CDATA[ ]]> for things like script and style elements
     * @param element
     * @return true if <[CDATA[ ]]> should be used.
     */
    protected boolean dontEscape(Element element) {
        // make sure <script src=..></script> doesn't get turned into <script src=..><[CDATA[]]></script>
        // TODO check for blank content as well.
        return props.isUseCdataForScriptAndStyle() && isScriptOrStyle(element) && !element.hasChildNodes();
    }
    private void createSubnodes(Document document, Element element, List tagChildren) {
        if (tagChildren != null) {
            for(Object item : tagChildren) {
                if (item instanceof CommentNode) {
                    CommentNode commentNode = (CommentNode) item;
                    Comment comment = document.createComment( commentNode.getContent() );
                    element.appendChild(comment);
                } else if (item instanceof ContentNode) {
                    ContentNode contentNode = (ContentNode) item;
                    String content = contentNode.getContent();
                    boolean specialCase = dontEscape(element);
                    if (escapeXml && !specialCase) {
                        content = Utils.escapeXml(content, props, true);
                    }
                    element.appendChild( specialCase ? document.createCDATASection(content) : document.createTextNode(content) );
                } else if (item instanceof TagNode) {
                    TagNode subTagNode = (TagNode) item;
                    Element subelement = document.createElement( subTagNode.getName() );
                    Map attributes =  subTagNode.getAttributes();
                    Iterator entryIterator = attributes.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) entryIterator.next();
                        String attrName = (String) entry.getKey();
                        String attrValue = (String) entry.getValue();
                        if (escapeXml) {
                            attrValue = Utils.escapeXml(attrValue, props, true);
                        }
                        subelement.setAttribute(attrName, attrValue);
                    }

                    // recursively create subnodes
                    createSubnodes(document, subelement, subTagNode.getAllChildren());

                    element.appendChild(subelement);
                } else if (item instanceof List) {
                    List sublist = (List) item;
                    createSubnodes(document, element, sublist);
                }
            }
        }
    }

}