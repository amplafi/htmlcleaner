package org.htmlcleaner;

/**
 * @author patmoore
 *
 */
public enum CloseTag {
    /**
     * <div></div> is required. Minimizing to <div/> is not permitted.
     */
    required(false, true),
    /**
     * <hr> or <hr/> is permitted
     */
    optional(true, true),
    /**
     * <img/> is not permitted
     */
    forbidden(true, false);
    private final boolean minimizedTagPermitted;
    private final boolean endTagPermitted;
    /**
     *
     * @param minimizedTagPermitted if true tag can be reduced to <x/>
     * @param endTagPermitted TODO
     */
    private CloseTag(boolean minimizedTagPermitted, boolean endTagPermitted) {
        this.minimizedTagPermitted = minimizedTagPermitted;
        this.endTagPermitted =endTagPermitted;
    }

    /**
     * @return
     */
    public boolean isMinimizedTagPermitted() {
        return this.minimizedTagPermitted;
    }

    /**
     * @return the endTagPermitted
     */
    public boolean isEndTagPermitted() {
        return endTagPermitted;
    }
}
