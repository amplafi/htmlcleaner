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

        props.setTransResCharsToNCR(false);
//        props.setIgnoreQuestAndExclam(true);
        props.setUseCdataForScriptAndStyle(false);
        props.setRecognizeUnicodeChars(true);
        props.setTranslateSpecialEntities(true);
        props.setTransSpecialEntitiesToNCR(false);
        props.setUseEmptyElementTags(false);
        props.setOmitXmlDeclaration(true);
        props.setOmitDoctypeDeclaration(false);
        props.setNamespacesAware(true);

        long start = System.currentTimeMillis();
        TagNode node = cleaner.clean(new File("c:/temp/htmlcleanertest/b92.htm"), "UTF-8");
        System.out.println("Cleanup time: " + (System.currentTimeMillis() - start));
    }

}