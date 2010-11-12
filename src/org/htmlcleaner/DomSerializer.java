package org.htmlcleaner;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>DOM serializer - creates xml DOM.</p>
 *
 * Created by: Vladimir Nikic<br/>
 * Date: April, 2007.
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

        Document document = factory.newDocumentBuilder().newDocument();
        Element rootElement = document.createElement(rootNode.getName());
        document.appendChild(rootElement);

        createSubnodes(document, rootElement, rootNode.getChildren());

        return document;
    }

    private void createSubnodes(Document document, Element element, List tagChildren) {
        if (tagChildren != null) {
            Iterator it = tagChildren.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                if (item instanceof CommentToken) {
                    CommentToken commentToken = (CommentToken) item;
                    Comment comment = document.createComment( commentToken.getContent() );
                    element.appendChild(comment);
                } else if (item instanceof ContentToken) {
                    String nodeName = element.getNodeName();
                    ContentToken contentToken = (ContentToken) item;
                    String content = contentToken.getContent();
                    boolean specialCase = props.isUseCdataForScriptAndStyle() &&
                                          ("script".equalsIgnoreCase(nodeName) || "style".equalsIgnoreCase(nodeName));
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
                    createSubnodes(document, subelement, subTagNode.getChildren());

                    element.appendChild(subelement);
                } else if (item instanceof List) {
                    List sublist = (List) item;
                    createSubnodes(document, element, sublist);
                }
            }
        }
    }

}