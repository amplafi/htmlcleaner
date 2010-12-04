package org.htmlcleaner;

import com.sun.org.apache.xml.internal.serialize.*;
import org.jdom.*;
import org.jdom.Document;
import org.jdom.output.*;
import org.w3c.dom.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.File;

/**
 * Vladimir Nikic
 * Date: Apr 13, 2007
 */
public class Working {

    public static void main(String[] args) throws IOException, XPatherException, ParserConfigurationException {
        String html = "<script src=\"a\" type=\"text/javascript\" /><script src=\"b\" type=\"text/javascript\"/>";
        final HtmlCleaner cleaner = new HtmlCleaner();
        final CleanerProperties props = cleaner.getProperties();

//        final String resources[] = {
//                "http://www.b92.net",
//                "http://www.nba.com",
//                "http://www.naslovi.net/",
//                "http://www.theserverside.com/",
//                "http://www.yahoo.com",
//        };
//        final String resources[] = {
//                "c:/temp/htmlcleanertest/1.htm",
//                "c:/temp/htmlcleanertest/2.htm",
//                "c:/temp/htmlcleanertest/3.htm",
//                "c:/temp/htmlcleanertest/4.htm",
//                "c:/temp/htmlcleanertest/5.htm",
//        };

//        props.setTransResCharsToNCR(false);
//        props.setIgnoreQuestAndExclam(true);
        props.setUseCdataForScriptAndStyle(false);
        props.setRecognizeUnicodeChars(true);
        props.setTranslateSpecialEntities(true);
        props.setTransSpecialEntitiesToNCR(true);
        props.setUseEmptyElementTags(false);
        props.setOmitXmlDeclaration(true);
        props.setOmitDoctypeDeclaration(false);
        props.setNamespacesAware(true);

        long start = System.currentTimeMillis();

        final String urlToTest = "http://htmlcleaner.sf.net/";
        TagNode node = cleaner.clean(new File("c:/temp/htmlcleanertest/mama.html"), "UTF-8");
//        TagNode node = cleaner.clean(new URL(urlToTest));

        System.out.println("Cleanup time: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        node.traverse(new TagNodeVisitor() {
            public boolean visit(TagNode parentNode, HtmlNode node) {
                if (node instanceof TagNode) {
                    TagNode tagNode = (TagNode) node;
                    if ( "a".equals(tagNode.getName()) || "link".equals(tagNode.getName()) ) {
                        String href = tagNode.getAttributeByName("href");
                        if (href != null) {
                            tagNode.setAttribute("href", Utils.fullUrl(urlToTest, href));
                        }
                    } else if ( "img".equals(tagNode.getName()) ) {
                        String src = tagNode.getAttributeByName("src");
                        if (src != null) {
                            tagNode.setAttribute("src", Utils.fullUrl(urlToTest, src));
                        }
                    } else if ("b".equals(tagNode.getName())) {
                        TagNode spanNode = new TagNode("span");
                        spanNode.setAttribute("style", "font-weight:bold;");
                        spanNode.addChild(new ContentNode("BOLD: "));
                        spanNode.addChildren(tagNode.getChildren());
                        parentNode.replaceChild(tagNode, spanNode);
                    } else if ("h4".equals(tagNode.getName())) {
                        System.out.println("H4 index: " + parentNode.getChildIndex(tagNode));
                        parentNode.insertChild(0, new CommentNode("very first comment"));
                        parentNode.insertChildBefore(tagNode, new CommentNode("before H4"));
                        parentNode.insertChildAfter(tagNode, new CommentNode("after H4"));
                    }
                } else if (node instanceof ContentNode) {
                    StringBuilder content = ((ContentNode)node).getContent();
                    if (content.indexOf("one") >= 0) {
                        content.insert(0, "MY TEXT: ");
                    }
                } else if (node instanceof CommentNode) {
                    parentNode.removeChild(node);
                }

                return true;
            }
        });

        System.out.println("Traverse time: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

//        props.setNamespacesAware(false);
        new SimpleHtmlSerializer(props).writeToFile(node, "c:/temp/htmlcleanertest/simplemamaout.html", "utf-8");
        new CompactHtmlSerializer(props).writeToFile(node, "c:/temp/htmlcleanertest/compactmamaout.html", "utf-8");
        new PrettyHtmlSerializer(props).writeToFile(node, "c:/temp/htmlcleanertest/prettymamaout.html", "utf-8");
        new PrettyXmlSerializer(props).writeToFile(node, "c:/temp/htmlcleanertest/prettymamaout.xml", "utf-8");

        System.out.println("Serialize time: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        Document jDom = new JDomSerializer(props).createJDom(node);
        new XMLOutputter(Format.getPrettyFormat()).output(jDom, System.out);

        org.w3c.dom.Document dom = new DomSerializer(props).createDOM(node);
        OutputFormat of = new OutputFormat("XML","utf-8",true);
        of.setIndent(1);
        of.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(System.out, of);
        serializer.asDOMSerializer();
        serializer.serialize(dom);


//        for (int i = 0; i < resources.length; i++) {
//            TagNode node = cleaner.clean(new URL(resources[i]));
//            prettySerializer.writeXmlToFile(node, "c:/temp/htmlcleanertest/out/" + i + ".xml", "UTF-8");
//        }
//        System.out.println("Vreme u jednom tredu: " + (System.currentTimeMillis() - start));
//
//        final long start1 = System.currentTimeMillis();
//
//        for (int i = 0; i < resources.length; i++) {
//            final int index = i;
//            new Thread(new Runnable() {
//                public void run() {
//                    try {
//                        TagNode node = cleaner.clean(new URL(resources[index]));
//                        prettySerializer.writeXmlToFile(node, "c:/temp/htmlcleanertest/out/" + index + "a.xml", "UTF-8");
//                        System.out.println("Vreme u tredu " + index + ": " + (System.currentTimeMillis() - start1));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }

//        TagNode node = cleaner.clean(new File("c:/temp/test.html"));
//        prettySerializer.writeXmlToFile(node, "c:/temp/htmlcleanertest/1.xml", "utf-8");
    }

}