package org.htmlcleaner;

class TestTagNodeUtils {
    public static TagNode getTagNode(CleanerProperties cleanerProperties, String textContent) {
        HtmlCleaner htmlCleaner = new HtmlCleaner(cleanerProperties);
        TagNode tagNode = htmlCleaner.clean(textContent);
        return tagNode;
    }
}
