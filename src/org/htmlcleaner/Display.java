package org.htmlcleaner;

/**
 * Most HTML 4 elements permitted within the BODY are classified as either
 * block-level elements or inline elements. This enumeration contains
 * corresponding constants to distinguish them.
 * 
 * @author Konstantin Burov (aectann@gmail.com)
 * 
 */
public enum Display {
	/**
	 * Block-level elements typically contain inline elements and other
	 * block-level elements. When rendered visually, block-level elements
	 * usually begin on a new line.
	 */
	block,
	/**
	 * Inline elements typically may only contain text and other inline
	 * elements. When rendered visually, inline elements do not usually begin on
	 * a new line.
	 */
	inline,

	/**
	 * The following elements may be used as either block-level elements or
	 * inline elements. If used as inline elements (e.g., within another inline
	 * element or a P), these elements should not contain any block-level
	 * elements.
	 */
	any,

	/**
	 * Elements that are not actually inline or block, usually such elements are
	 * not rendered at all rendered.
	 */
	none
}
