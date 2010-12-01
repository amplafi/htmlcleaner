package org.htmlcleaner;

import org.jdom.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>DOM serializer - creates xml DOM.</p>
 * 
 * Created by: Vladimir Nikic, Benson Margulies <br/>
 * Date: April, 2007.
 */
public class JDomSerializer {

    private DefaultJDOMFactory factory;

    protected CleanerProperties props;
    protected boolean escapeXml = true;

    private Map<String, Namespace> namespaces = new HashMap<String, Namespace>();

    public JDomSerializer(CleanerProperties props, boolean escapeXml) {
        this.props = props;
        this.escapeXml = escapeXml;
    }

    public JDomSerializer(CleanerProperties props) {
        this(props, true);
    }

    public Document createJDom(TagNode rootNode) {
        this.factory = new DefaultJDOMFactory();
        Element rootElement = createElement(rootNode);
        Document document = this.factory.document(rootElement);
        
        for (Map.Entry<String, String> entry: rootNode.getAttributes().entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (escapeXml) {
                attrValue = Utils.escapeXml(attrValue, props, true);
            }
            setAttribute(rootElement, attrName, attrValue);
        }

        createSubnodes(rootElement, rootNode.getChildren());

        return document;
    }

    private Element createElement(TagNode node) {
        String name = node.getName();
        String prefix = Utils.getXmlNSPrefix(name);
        if (prefix != null) {
            return factory.element(Utils.getXmlName(name), namespaces.get(prefix));
        } else {
            return factory.element(name);
        }
    }

    private void setAttribute(Element element, String attrName, String attrValue) {
        if (attrName.equals("xmlns")) {
            Namespace namespace = Namespace.getNamespace(attrValue);
            namespaces.put("", namespace);
            element.addNamespaceDeclaration(namespace);
        } else if (attrName.startsWith("xmlns:")) {
            String prefix = attrName.substring(6);
            Namespace namespace = Namespace.getNamespace(prefix, attrValue);
            namespaces.put(prefix, namespace);
            element.addNamespaceDeclaration(namespace);
        } else {
            String prefix = Utils.getXmlNSPrefix(attrName);
            if (prefix != null) {
                element.setAttribute( Utils.getXmlName(attrName), attrValue, namespaces.get(prefix) );
            } else {
                element.setAttribute(attrName, attrValue);
            }
        }
    }

    private void createSubnodes(Element element, List tagChildren) {
        if (tagChildren != null) {
            Iterator it = tagChildren.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                if (item instanceof CommentToken) {
                    CommentToken commentToken = (CommentToken) item;
                    Comment comment = factory.comment( commentToken.getContent().toString() );
                    element.addContent(comment);
                } else if (item instanceof ContentToken) {
                    String nodeName = element.getName();
                    String content = item.toString();
                    boolean specialCase = props.isUseCdataForScriptAndStyle() &&
                                          ("script".equalsIgnoreCase(nodeName) || "style".equalsIgnoreCase(nodeName));                    
                    if (escapeXml && !specialCase) {
                        content = Utils.escapeXml(content, props, true);
                    }
                    Text text = specialCase ? factory.cdata(content) : factory.text(content);
                    element.addContent(text);
                } else if (item instanceof TagNode) {
                    TagNode subTagNode = (TagNode) item;
                    Element subelement = createElement(subTagNode);
                    for (Map.Entry<String, String> entry: subTagNode.getAttributes().entrySet()) {
                        String attrName = entry.getKey();
                        String attrValue = entry.getValue();
                        setAttribute(subelement, attrName, attrValue);
                    }

                    // recursively create subnodes
                    createSubnodes(subelement, subTagNode.getChildren());

                    element.addContent(subelement);
                } else if (item instanceof List) {
                    List sublist = (List) item;
                    createSubnodes(element, sublist);
                }
            }
        }
    }

}