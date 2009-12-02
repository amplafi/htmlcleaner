package org.htmlcleaner;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class RandomPageTest extends TestCase {
	
	public void testPage() throws IOException {
		HtmlCleaner cleaner = new HtmlCleaner();
        cleaner.clean( new File("test/org/htmlcleaner/files/gg_prob.html") );		
	}

}
