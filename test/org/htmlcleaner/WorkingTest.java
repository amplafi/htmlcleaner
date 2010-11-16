package org.htmlcleaner;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.htmlcleaner.*;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 * Vladimir Nikic
 * Date: Apr 13, 2007
 */
public class WorkingTest {

    public static void main(String[] args) throws IOException, XPatherException, ParserConfigurationException {
        long start = System.currentTimeMillis();

//        long time = System.currentTimeMillis();
//        TagNode x = rootNode.findElementByName("a", true);
////        cleaner.setInnerHtml(x, "<tr>mama</tr><td>seka<td>bata");
////        System.out.println("***" + cleaner.getInnerHtml(x) + "***");
//        System.out.println("search time: " + (System.currentTimeMillis() - time));

//        for (int i = 0; i < x.length; i++) {
//            TagNode tagNode = x[i];
//            tagNode.removeFromTree();
//        }
//        System.out.println(new SimpleXmlSerializer(cleaner.getProperties()).getXmlAsString(rootNode));
        // writeXmlToFile(rootNode, "c:/temp/out.xml", "UTF-8")
//        Object z[] = rootNode.evaluateXPath("data( //a['v' < @id] )");
//        System.out.println("-->" + z.length);


        String html = "<script src=\"a\" type=\"text/javascript\" /><script src=\"b\" type=\"text/javascript\"/>";
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setOmitUnknownTags(true);
        props.setUseCdataForScriptAndStyle(true);
        props.setRecognizeUnicodeChars(false);
        props.setUseEmptyElementTags(true);
        props.setAdvancedXmlEscape(true);
        props.setTranslateSpecialEntities(true);
        props.setBooleanAttributeValues("empty");
        props.setNamespacesAware(false);

//        TagNode node = cleaner.clean(html);
        TagNode node = cleaner.clean(new File("c:/temp/b92.html"));
//        TagNode node = cleaner.clean(new URL("http://www.youtube.com/"));
//        cleaner.setInnerHtml( (TagNode)(node.evaluateXPath("//table[1]")[0]), "<td>row1<td>row2<td>row3");
//        Document document = new JDomSerializer(props).createJDom(node);
//        XMLOutputter xmlOut = new XMLOutputter();
//        xmlOut.output(document, System.out);

//        System.out.println( new PrettyXmlSerializer(props).getXmlAsString(node) );

        System.out.println("vreme: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        new PrettyXmlSerializer(props).writeXmlToFile(node, "c:/temp/htmlcleanertest/1.xml", "UTF-8");
//        new PrettyXmlSerializer(props).writeXmlToStream(node, System.out);
        System.out.println("vreme: " + (System.currentTimeMillis() - start));

        new ConfigFileTagProvider(new File("default.xml"));


        System.out.println("vreme: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        Document document = new DomSerializer(props, true).createDOM(node);

        System.out.println("vreme: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        org.jdom.Document jDom = new JDomSerializer(props, true).createJDom(node);
    }

}