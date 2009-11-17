package org.htmlcleaner.audit;

/**
 * Possible error codes (read messages) that cleaner uses to inform clients about
 * reasons/actions that modification involves.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 */
public enum ErrorType {

    /**
     * Tag which existence is critical for the current is missing. Most likely, current tag was pruned.
     */
    FatalTagMissing,
    /**
     * The tag wasn't found on list of allowed tags, thus it was removed.
     */
    NotAllowedTag,
    /**
     * Missing parent tag was added for current (i.e. tbody for tr).
     */
    RequiredParentMissing,
    /**
     * No matching close token was found for the open tag. Tag was closed automatically.
     */
    UnclosedTag,
    /**
     * Second instance of an unique tag was found (i.e. second head), most likely it was removed.
     */
    UniqueTagDuplicated,
    /**
     * The tag was unknown or deprecated and current cleaner mode doesn't allows this. The tag was removed.
     */
    UnknownOrDeprecated,
    /**
     * This tag have bad child that shouldn't be here. Thus the tag is closed automatically to avoid such inclusion.
     */
    UnpermittedChild
}
