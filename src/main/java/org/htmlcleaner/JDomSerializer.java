package org.htmlcleaner;

import org.jdom.*;

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

    public JDomSerializer(CleanerProperties props, boolean escapeXml) {
        this.props = props;
        this.escapeXml = escapeXml;
    }

    public JDomSerializer(CleanerProperties props) {
        this(props, true);
    }

    public Document createJDom(TagNode rootNode) {
        this.factory = new DefaultJDOMFactory();
        Element rootElement = this.factory.element(rootNode.getName());
        Document document = this.factory.document(rootElement);
        createSubnodes(rootElement, rootNode.getChildren());

        return document;
    }

    private void createSubnodes(Element element, List tagChildren) {
        if (tagChildren != null) {
            Iterator it = tagChildren.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                if (item instanceof CommentToken) {
                    CommentToken commentToken = (CommentToken) item;
                    Comment comment = factory.comment( commentToken.getContent() );
                    element.addContent(comment);
                } else if (item instanceof ContentToken) {
                    String nodeName = element.getName();
                    ContentToken contentToken = (ContentToken) item;
                    String content = contentToken.getContent();
                    boolean specialCase = props.isUseCdataForScriptAndStyle() &&
                                          ("script".equalsIgnoreCase(nodeName) || "style".equalsIgnoreCase(nodeName));                    
                    if (escapeXml && !specialCase) {
                        content = Utils.escapeXml(content, props, true);
                    }
                    Text text = specialCase ? factory.cdata(content) : factory.text(content);
                    element.addContent(text);
                } else if (item instanceof TagNode) {
                    TagNode subTagNode = (TagNode) item;
                    Element subelement = factory.element( subTagNode.getName() );
                    Map attributes = subTagNode.getAttributes();
                    Iterator entryIterator = attributes.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) entryIterator.next();
                        String attrName = (String) entry.getKey();
                        String attrValue = (String) entry.getValue();
                        subelement.setAttribute(attrName, attrValue);
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