package org.htmlcleaner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for some common use of <script> tags within <head> elements
 * @author scottw
 *
 */
public class ScriptTest {
	
	@Test
	public void getScripts() throws IOException{
	    HtmlCleaner cleaner = new HtmlCleaner();
        TagNode html = cleaner.clean( new File("src/test/resources/script_test.html") );
        TagNode head = html.findElementByName("head", false);
        
        ArrayList<TagNode> scripts = new ArrayList<TagNode>();
		List<TagNode> children = head.getChildTagList();	
		
		for(TagNode child : children){						
			if(child.getName().equals("script")){				
				scripts.add(child);
			}			
		}
		assertEquals(3, scripts.size());
		assertEquals("x.js", scripts.get(0).getAttributeByName("src"));
		assertEquals("y.js", scripts.get(1).getAttributeByName("src"));
		assertEquals("z.js", scripts.get(2).getAttributeByName("src"));

	}

}
