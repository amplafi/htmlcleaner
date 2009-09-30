/*  Copyright (c) 2006-2007, Vladimir Nikic
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

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "HtmlCleaner" in the
    subject line.
*/

package org.htmlcleaner;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Properties defining cleaner's behaviour
 *
 * Created by: Vladimir Nikic<br/>
 * Date: March, 2008.
 */
public class CleanerProperties {

//    public static final String DEFAULT_CHARSET = System.getProperty("file.encoding");
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String BOOL_ATT_SELF = "self";
    public static final String BOOL_ATT_EMPTY = "empty";
    public static final String BOOL_ATT_TRUE = "true";

    private ITagInfoProvider tagInfoProvider;
    /**
     * If this parameter is set to true, ampersand sign (&) that proceeds valid XML character sequences (&XXX;) will not be escaped with &amp;XXX; 
     */
    private boolean advancedXmlEscape;
    private boolean useCdataForScriptAndStyle;
    private boolean translateSpecialEntities;
    private boolean recognizeUnicodeChars;
    private boolean omitUnknownTags;
    private boolean treatUnknownTagsAsContent;
    private boolean omitDeprecatedTags;
    private boolean omitComments;
    private boolean treatDeprecatedTagsAsContent;
    private OptionalOutput omitXmlDeclaration;
    private OptionalOutput omitDoctypeDeclaration;
    private OptionalOutput omitHtmlEnvelope;
    private boolean useEmptyElementTags;
    private boolean allowMultiWordAttributes;
    private String booleanAttributeValues;
    private boolean ignoreQuestAndExclam;
    private boolean allowHtmlInsideAttributes;
    private boolean namespacesAware;
    private String hyphenReplacementInComment;
    // comma separate list of tags pruned.
    private String pruneTags;
    // comma separate list of tags allowed.
    private String allowTags;
    /**
     * if true then, remove html elements that have no meaningful effect:
     * <ul>
     * <li>empty (or encapsulates only blanks ) span, b, u, i, strong, etc. elements that are used to format text. If an element has an id or class it is not el</li>
     * <li>br elements just before a block closing ( &lt;/div> for example ), or just after a block opening element ( &lt;body> as another example )</l>
     *  </ul>
     *  
     *  TODO: Konstantin -- use TagTransformation ( you will have to do some refactoring )  
     */
    private CollapseHtml collapseNullHtml;
	private CleanerTransformations cleanerTransformations = new CleanerTransformations();
    /**
     * blacklist of tags
     */
    private Set pruneTagSet = new HashSet();
    /**
     * the list of allowed tags (whitelist approach v. blacklist approach of pruneTags )
     */
    private Set allowTagSet = new HashSet();
    private String charset;

    public CleanerProperties() {
        reset();
    }
    
    /**
     * @param tagInfoProvider
     */
    public CleanerProperties(ITagInfoProvider tagInfoProvider) {
        reset();
        this.tagInfoProvider = tagInfoProvider;
    }

    /**
     * @param tagInfoProvider the tagInfoProvider to set
     */
    void setTagInfoProvider(ITagInfoProvider tagInfoProvider) {
        this.tagInfoProvider = tagInfoProvider;
    }

    public ITagInfoProvider getTagInfoProvider() {
        return tagInfoProvider;
    }

    public boolean isAdvancedXmlEscape() {
        return advancedXmlEscape;
    }

    public void setAdvancedXmlEscape(boolean advancedXmlEscape) {
        this.advancedXmlEscape = advancedXmlEscape;
    }

    public CollapseHtml getCollapseNullHtml() {
    	return collapseNullHtml;
    }
    
    public void setCollapseNullHtml(CollapseHtml collapseNullHtml) {
    	this.collapseNullHtml = collapseNullHtml;
    }

    public boolean isUseCdataForScriptAndStyle() {
        return useCdataForScriptAndStyle;
    }

    public void setUseCdataForScriptAndStyle(boolean useCdataForScriptAndStyle) {
        this.useCdataForScriptAndStyle = useCdataForScriptAndStyle;
    }

    public boolean isTranslateSpecialEntities() {
        return translateSpecialEntities;
    }

    /**
     * TODO : use {@link OptionalOutput}
     * @param translateSpecialEntities
     */
    public void setTranslateSpecialEntities(boolean translateSpecialEntities) {
        this.translateSpecialEntities = translateSpecialEntities;
    }

    public boolean isRecognizeUnicodeChars() {
        return recognizeUnicodeChars;
    }

    public void setRecognizeUnicodeChars(boolean recognizeUnicodeChars) {
        this.recognizeUnicodeChars = recognizeUnicodeChars;
    }

    public boolean isOmitUnknownTags() {
        return omitUnknownTags;
    }

    public void setOmitUnknownTags(boolean omitUnknownTags) {
        this.omitUnknownTags = omitUnknownTags;
    }

    public boolean isTreatUnknownTagsAsContent() {
        return treatUnknownTagsAsContent;
    }

    public void setTreatUnknownTagsAsContent(boolean treatUnknownTagsAsContent) {
        this.treatUnknownTagsAsContent = treatUnknownTagsAsContent;
    }

    public boolean isOmitDeprecatedTags() {
        return omitDeprecatedTags;
    }

    public void setOmitDeprecatedTags(boolean omitDeprecatedTags) {
        this.omitDeprecatedTags = omitDeprecatedTags;
    }

    public boolean isTreatDeprecatedTagsAsContent() {
        return treatDeprecatedTagsAsContent;
    }

    public void setTreatDeprecatedTagsAsContent(boolean treatDeprecatedTagsAsContent) {
        this.treatDeprecatedTagsAsContent = treatDeprecatedTagsAsContent;
    }

    public boolean isOmitComments() {
        return omitComments;
    }

    public void setOmitComments(boolean omitComments) {
        this.omitComments = omitComments;
    }

    public boolean isOmitXmlDeclaration() {
        return omitXmlDeclaration == OptionalOutput.omit;
    }

    public void setOmitXmlDeclaration(boolean omitXmlDeclaration) {
        this.omitXmlDeclaration = omitXmlDeclaration?OptionalOutput.omit:OptionalOutput.alwaysOutput;
    }

    /**
     * 
     * @return also return true if omitting the Html Envelope
     */
    public boolean isOmitDoctypeDeclaration() {
        return omitDoctypeDeclaration == OptionalOutput.omit || isOmitHtmlEnvelope();
    }

    public void setOmitDoctypeDeclaration(boolean omitDoctypeDeclaration) {
        this.omitDoctypeDeclaration = omitDoctypeDeclaration?OptionalOutput.omit:OptionalOutput.alwaysOutput;
    }

    public boolean isOmitHtmlEnvelope() {
        return omitHtmlEnvelope == OptionalOutput.omit;
    }

    public void setOmitHtmlEnvelope(boolean omitHtmlEnvelope) {
        this.omitHtmlEnvelope = omitHtmlEnvelope?OptionalOutput.omit:OptionalOutput.alwaysOutput;
    }

    public boolean isUseEmptyElementTags() {
        return useEmptyElementTags;
    }

    public void setUseEmptyElementTags(boolean useEmptyElementTags) {
        this.useEmptyElementTags = useEmptyElementTags;
    }

    public boolean isAllowMultiWordAttributes() {
        return allowMultiWordAttributes;
    }

    public void setAllowMultiWordAttributes(boolean allowMultiWordAttributes) {
        this.allowMultiWordAttributes = allowMultiWordAttributes;
    }

    public boolean isAllowHtmlInsideAttributes() {
        return allowHtmlInsideAttributes;
    }

    public void setAllowHtmlInsideAttributes(boolean allowHtmlInsideAttributes) {
        this.allowHtmlInsideAttributes = allowHtmlInsideAttributes;
    }

    public boolean isIgnoreQuestAndExclam() {
        return ignoreQuestAndExclam;
    }

    public void setIgnoreQuestAndExclam(boolean ignoreQuestAndExclam) {
        this.ignoreQuestAndExclam = ignoreQuestAndExclam;
    }

    public boolean isNamespacesAware() {
        return namespacesAware;
    }

    public void setNamespacesAware(boolean namespacesAware) {
        this.namespacesAware = namespacesAware;
    }

    public String getHyphenReplacementInComment() {
        return hyphenReplacementInComment;
    }

    public void setHyphenReplacementInComment(String hyphenReplacementInComment) {
        this.hyphenReplacementInComment = hyphenReplacementInComment;
    }

    public String getPruneTags() {
        return pruneTags;
    }

    public void setPruneTags(String pruneTags) {
        this.pruneTags = pruneTags;
        this.setPruneTagSet(pruneTags);
    }
    
    private void setPruneTagSet(String pruneTags) {
        pruneTagSet.clear();
        addTagNameConditions(pruneTagSet, pruneTags);
    }
    public Set getPruneTagSet() {
        Set copy = new HashSet(pruneTagSet);
        copy.add(TagNodeAutoGeneratedCondition.INSTANCE);
        addCollapseConditions(copy);
        return copy;
    }
    
    public String getAllowTags() {
        return allowTags;
    }

    public void setAllowTags(String allowTags) {
        this.allowTags = allowTags;
        this.setAllowTagSet(allowTags);
    }

    private void setAllowTagSet(String allowTags) {
        allowTagSet.clear();
        addTagNameConditions(allowTagSet, allowTags);
    }

    /**
     * @param tagSet 
     * @param tagsNameStr
     */
    private void addTagNameConditions(Set tagSet, String tagsNameStr) {
        if (tagsNameStr != null) {
            StringTokenizer tokenizer = new StringTokenizer(tagsNameStr, ",");
            while ( tokenizer.hasMoreTokens() ) {
                tagSet.add( new TagNode.TagNodeNameCondition(tokenizer.nextToken().trim().toLowerCase()) );
            }
        }
    }
    
    private void addCollapseConditions(Set tagSet) {
        switch(this.collapseNullHtml) {
        case none:
            break;
        case emptyOrBlankInlineElements:
            tagSet.add(new TagNodeEmptyContentCondition(this.tagInfoProvider));
            tagSet.add(new TagNodeInsignificantBrCondition());
        }
    }
    public Set getAllowTagSet() {
        return allowTagSet;
    }

    /**
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    public String getBooleanAttributeValues() {
        return booleanAttributeValues;
    }

    public void setBooleanAttributeValues(String booleanAttributeValues) {
        if ( BOOL_ATT_SELF.equalsIgnoreCase(booleanAttributeValues) ||
             BOOL_ATT_EMPTY.equalsIgnoreCase(booleanAttributeValues) ||
             BOOL_ATT_TRUE.equalsIgnoreCase(booleanAttributeValues) ) {
            this.booleanAttributeValues = booleanAttributeValues.toLowerCase();
        } else {
            this.booleanAttributeValues = BOOL_ATT_SELF;
        }
    }

    /**
     * advancedXmlEscape = true;
     * useCdataForScriptAndStyle = true;
     * translateSpecialEntities = true;
     * recognizeUnicodeChars = true; 
     * omitUnknownTags = false; 
     * treatUnknownTagsAsContent = false;
     * omitDeprecatedTags = false;
     * treatDeprecatedTagsAsContent = false; 
     * omitComments = false;
     * omitXmlDeclaration = OptionalOutput.alwaysOutput; 
     * omitDoctypeDeclaration = OptionalOutput.alwaysOutput; 
     * omitHtmlEnvelope = OptionalOutput.alwaysOutput;
     * useEmptyElementTags = true; 
     * allowMultiWordAttributes = true; 
     * allowHtmlInsideAttributes = false; 
     * ignoreQuestAndExclam = false; 
     * namespacesAware = true; 
     * hyphenReplacementInComment = "="; 
     * pruneTags = null; 
     * allowTags = null;
     * booleanAttributeValues = BOOL_ATT_SELF; 
     * collapseNullHtml = CollapseHtml.none
     * charset = "UTF-8";
     */
    public void reset() {
        advancedXmlEscape = true;
        useCdataForScriptAndStyle = true;
        translateSpecialEntities = true;
        recognizeUnicodeChars = true;
        omitUnknownTags = false;
        treatUnknownTagsAsContent = false;
        omitDeprecatedTags = false;
        treatDeprecatedTagsAsContent = false;
        omitComments = false;
        omitXmlDeclaration = OptionalOutput.alwaysOutput;
        omitDoctypeDeclaration = OptionalOutput.alwaysOutput;
        omitHtmlEnvelope = OptionalOutput.alwaysOutput;
        useEmptyElementTags = true;
        allowMultiWordAttributes = true;
        allowHtmlInsideAttributes = false;
        ignoreQuestAndExclam = false;
        namespacesAware = true;
        hyphenReplacementInComment = "=";
        setPruneTags(null);
        setAllowTags(null);
        booleanAttributeValues = BOOL_ATT_SELF;
        charset = "UTF-8";
        cleanerTransformations.clear();
        collapseNullHtml = CollapseHtml.none;
        tagInfoProvider = DefaultTagProvider.getInstance();
    }

    /**
     * @return the cleanerTransformations
     */
    public CleanerTransformations getCleanerTransformations() {
        return cleanerTransformations;
    }
    public void setCleanerTransformations(CleanerTransformations cleanerTransformations) {
        if ( cleanerTransformations == null ) {
            this.cleanerTransformations.clear();
        } else {
            this.cleanerTransformations = cleanerTransformations;
        }
    }
}