/*  Copyright (c) 2006-2013, HtmlCleaner Team (Vladimir Nikic, Pat Moore, Scott Wilson)
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

*/
package org.htmlcleaner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.Serializer;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;

/**
 * Test case for determining whether HtmlCleaner is thread-safe.
 * 
 * Thanks to Tobias for the test case and report (see bug #86)
 *
 */
public class ThreadSafetyTest extends TestCase {
	
    private static final int NUM_THREADS = 20;
    private static final int NUM_RUNS = 100;
    private static final HtmlCleaner HTML_CLEANER;
    private static final Serializer SERIALIZER;
    private static final Pattern uidPattern = Pattern.compile(
        "\\b[A-F0-9]{8}(?:-[A-F0-9]{4}){3}-[A-Z0-9]{12}\\b", Pattern.CASE_INSENSITIVE);

    static {
        final CleanerProperties props = new CleanerProperties();
        props.setOmitDoctypeDeclaration(true);
        props.setOmitXmlDeclaration(true);
        props.setPruneTags("script");
        props.setTranslateSpecialEntities(true);
        props.setTransSpecialEntitiesToNCR(true);
        props.setTransResCharsToNCR(true);
        props.setRecognizeUnicodeChars(false);
        props.setUseEmptyElementTags(false);
        props.setIgnoreQuestAndExclam(false);
        props.setUseCdataForScriptAndStyle(false);
        props.setIgnoreQuestAndExclam(true);
        HTML_CLEANER = new HtmlCleaner(props);
        SERIALIZER = new SimpleHtmlSerializer(props);
    }

    public ThreadSafetyTest() {
        super();
    }

    public void testThreadSafety() throws Exception {
        Thread[] threads = new Thread[NUM_THREADS];
        CheckHtmlCleaner[] runnables = new CheckHtmlCleaner[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            runnables[i] = new CheckHtmlCleaner();
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
            if (false == runnables[i].errors.isEmpty()) {
                throw runnables[i].errors.get(0);
            }
        }
    }

    private static final class CheckHtmlCleaner implements Runnable {

        boolean onlyDetectForeignMarkers = true;

        List<AssertionError> errors = new ArrayList<AssertionError>();

        public void run() {
            for (int i = 0; i < NUM_RUNS; i++) {
                String marker = UUID.randomUUID().toString();
                String html =
                      "<html>\n"
                    + " <head>\n"
                    + "  <style type=\"text/css\">.mceResizeHandle {position: absolute;border: 1px solid black;background: #FFF;width: 5px;height: 5px;z-index: 10000}.mceResizeHandle:hover {background: #000}img[data-mce-selected] {outline: 1px solid black}img.mceClonedResizable, table.mceClonedResizable {position: absolute;outline: 1px dashed black;opacity: .5;z-index: 10000}\n"
                    + "</style>\n"
                    + " </head>\n"
                    + " <body style=\"\">\n"
                    + "  <div>\n"
                    + "   wurst\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   gurke\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   hund\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   " + marker +"\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   autobahn\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   suppe\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   &nbsp;\n"
                    + "  </div>\n"
                    + " </body>\n"
                    + "</html>"
                ;

                try {
                    TagNode htmlNode = HTML_CLEANER.clean(html);
                    StringWriter writer = new StringWriter();
                    SERIALIZER.write(htmlNode, writer, "UTF-8");
                    String cleanedHtml = writer.getBuffer().toString();
                    assertNotNull(cleanedHtml);
                    Matcher matcher = uidPattern.matcher(cleanedHtml);
                    if (onlyDetectForeignMarkers) {
                        if (matcher.find()) {
                            assertEquals("Cleaned HTML contains foreign marker", marker, matcher.group());
                        }
                    } else {
                        assertTrue("Cleaned HTML contains no marker", matcher.find());
                        assertEquals("Cleaned HTML contains foreign marker", marker, matcher.group());
                        assertTrue("Cleaned HTML appears to be too short", cleanedHtml.length() > 600);
                        assertTrue("Cleaned HTML appears to be too long", cleanedHtml.length() < 700);
                    }
                } catch (AssertionError e) {
                    errors.add(e);
                    break;
                } catch (RuntimeException e) {
                    // we want to find assertion errors
                    continue;
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

}
