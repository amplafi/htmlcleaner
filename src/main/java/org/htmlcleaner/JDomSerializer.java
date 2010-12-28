package org.htmlcleaner;

import org.jdom.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>JDom serializer - creates xml JDom instance out of the TagNode.</p>
 */
public class JDomSerializer {

    private DefaultJDOMFactory factory;

    protected CleanerProperties props;
    protected boolean escapeXml = true;

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

        setAttributes(rootNode, rootElement);

        createSubnodes(rootElement, rootNode.getChildren());

        return document;
    }

    private Element createElement(TagNode node) {
        String name = node.getName();
        boolean nsAware = props.isNamespacesAware();
        String prefix = Utils.getXmlNSPrefix(name);
        Map<String, String> nsDeclarations = node.getNamespaceDeclarations();
        String nsURI = null;
        if (prefix != null) {
            name = Utils.getXmlName(name);
            if (nsAware) {
                if (nsDeclarations != null) {
                    nsURI = nsDeclarations.get(prefix);
                }
                if (nsURI == null) {
                    nsURI = node.getNamespaceURIOnPath(prefix);
                }
                if (nsURI == null) {
                    nsURI = prefix;
                }
            }
        } else {
            if (nsAware) {
                if (nsDeclarations != null) {
                    nsURI = nsDeclarations.get("");
                }
                if (nsURI == null) {
                    nsURI = node.getNamespaceURIOnPath(prefix);
                }
            }
        }

        Element element;
        if (nsAware && nsURI != null) {
            Namespace ns = prefix == null ? Namespace.getNamespace(nsURI) : Namespace.getNamespace(prefix, nsURI);
            element = factory.element(name, ns);
        } else {
            element = factory.element(name);
        }

        if (nsAware) {
            defineNamespaceDeclarations(node, element);
        }
        return element;
    }

    private void defineNamespaceDeclarations(TagNode node, Element element) {
        Map<String, String> nsDeclarations = node.getNamespaceDeclarations();
        if (nsDeclarations != null) {
            for (Map.Entry<String, String> nsEntry: nsDeclarations.entrySet()) {
                String nsPrefix = nsEntry.getKey();
                String nsURI = nsEntry.getValue();
                Namespace ns = nsPrefix == null || "".equals(nsPrefix) ? Namespace.getNamespace(nsURI) : Namespace.getNamespace(nsPrefix, nsURI);
                element.addNamespaceDeclaration(ns);
            }
        }
    }

    private void setAttributes(TagNode node, Element element) {
        for (Map.Entry<String, String> entry: node.getAttributes().entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (escapeXml) {
                attrValue = Utils.escapeXml(attrValue, props, true);
            }
            String attPrefix = Utils.getXmlNSPrefix(attrName);
            Namespace ns = null;
            if (attPrefix != null) {
                attrName = Utils.getXmlName(attrName);
                if (props.isNamespacesAware()) {
                    String nsURI = node.getNamespaceURIOnPath(attPrefix);
                    if (nsURI == null) {
                        nsURI = attPrefix;
                    }
                    ns = Namespace.getNamespace(attPrefix, nsURI);
                }
            }
            if (ns == null) {
                element.setAttribute(attrName, attrValue);
            } else {
                element.setAttribute(attrName, attrValue, ns);
            }
        }
    }

    private void createSubnodes(Element element, List tagChildren) {
        if (tagChildren != null) {
            Iterator it = tagChildren.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                if (item instanceof CommentNode) {
                    CommentNode commentNode = (CommentNode) item;
                    Comment comment = factory.comment( commentNode.getContent().toString() );
                    element.addContent(comment);
                } else if (item instanceof ContentNode) {
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

                    setAttributes(subTagNode, subelement);

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