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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class contains map with special entities used in HTML and their
 * unicodes.</p>
 *
 * Created by: Vladimir Nikic<br/>
 * Date: November, 2006.
 */
public class SpecialEntities {

    public static final SpecialEntities INSTANCE = new SpecialEntities() {
        @Override
        @SuppressWarnings("unused") 
        public void put(SpecialEntity specialEntity) {
            throw new UnsupportedOperationException("cannot add to this instance");
        }
    };

    /**
     * key is the {@link SpecialEntity#getKey()} ( i.e. "quot" )
     */
	private Map entities = new HashMap();
	/**
	 * Key is the Integer returned by {@link SpecialEntity#intValue()}
	 */
	private Map entitiesByUnicodeCharcode = new HashMap();
	
	public SpecialEntities() {
		_put(new SpecialEntity("nbsp",	160, null, true));
		_put(new SpecialEntity("iexcl",	161, null, true));
		_put(new SpecialEntity("cent",	162, null, true));
		_put(new SpecialEntity("pound",	163, null, true));
		_put(new SpecialEntity("curren",	164, null, true));
		_put(new SpecialEntity("yen",		165, null, true));
		_put(new SpecialEntity("brvbar",	166, null, true));
		_put(new SpecialEntity("sect",	167, null, true));
		_put(new SpecialEntity("uml",		168, null, true));
		_put(new SpecialEntity("copy",	169, null, true));
		_put(new SpecialEntity("ordf",	170, null, true));
		_put(new SpecialEntity("laquo",	171, null, true));
		_put(new SpecialEntity("not",		172, null, true));
		_put(new SpecialEntity("shy",		173, null, true));
		_put(new SpecialEntity("reg",		174, null, true));
		_put(new SpecialEntity("macr",	175, null, true));
		_put(new SpecialEntity("deg",		176, null, true));
		_put(new SpecialEntity("plusmn",	177, null, true));
		_put(new SpecialEntity("sup2",	178, null, true));
		_put(new SpecialEntity("sup3",	179, null, true));
		_put(new SpecialEntity("acute",	180, null, true));
		_put(new SpecialEntity("micro",	181, null, true));
		_put(new SpecialEntity("para",	182, null, true));
		_put(new SpecialEntity("middot",	183, null, true));
		_put(new SpecialEntity("cedil",	184, null, true));
		_put(new SpecialEntity("sup1",	185, null, true));
		_put(new SpecialEntity("ordm",	186, null, true));
		_put(new SpecialEntity("raquo",	187, null, true));
		_put(new SpecialEntity("frac14",	188, null, true));
		_put(new SpecialEntity("frac12",	189, null, true));
		_put(new SpecialEntity("frac34",	190, null, true));
		_put(new SpecialEntity("iquest",	191, null, true));
		_put(new SpecialEntity("Agrave",	192, null, true));
		_put(new SpecialEntity("Aacute",	193, null, true));
		_put(new SpecialEntity("Acirc",	194, null, true));
		_put(new SpecialEntity("Atilde",	195, null, true));

		_put(new SpecialEntity("Auml",	196, null, true));
		_put(new SpecialEntity("Aring",	197, null, true));
		_put(new SpecialEntity("AElig",	198, null, true));
		_put(new SpecialEntity("Ccedil",	199, null, true));
		_put(new SpecialEntity("Egrave",	200, null, true));
		_put(new SpecialEntity("Eacute",	201, null, true));
		_put(new SpecialEntity("Ecirc",	202, null, true));
		_put(new SpecialEntity("Euml",	203, null, true));
		_put(new SpecialEntity("Igrave",	204, null, true));
		_put(new SpecialEntity("Iacute",	205, null, true));
		_put(new SpecialEntity("Icirc",	206, null, true));
		_put(new SpecialEntity("Iuml",	207, null, true));
		_put(new SpecialEntity("ETH",		208, null, true));
		_put(new SpecialEntity("Ntilde",	209, null, true));
		_put(new SpecialEntity("Ograve",	210, null, true));
		_put(new SpecialEntity("Oacute",	211, null, true));
		_put(new SpecialEntity("Ocirc",	212, null, true));
		_put(new SpecialEntity("Otilde",	213, null, true));
		_put(new SpecialEntity("Ouml",	214, null, true));
		_put(new SpecialEntity("times",	215, null, true));
		_put(new SpecialEntity("Oslash",	216, null, true));
		_put(new SpecialEntity("Ugrave",	217, null, true));
		_put(new SpecialEntity("Uacute",	218, null, true));
		_put(new SpecialEntity("Ucirc",	219, null, true));
		_put(new SpecialEntity("Uuml",	220, null, true));
		_put(new SpecialEntity("Yacute",	221, null, true));
		_put(new SpecialEntity("THORN",	222, null, true));
		_put(new SpecialEntity("szlig",	223, null, true));
		_put(new SpecialEntity("agrave",	224, null, true));
		_put(new SpecialEntity("aacute",	225, null, true));
		_put(new SpecialEntity("acirc",	226, null, true));
		_put(new SpecialEntity("atilde",	227, null, true));
		_put(new SpecialEntity("auml",	228, null, true));
		_put(new SpecialEntity("aring",	229, null, true));
		_put(new SpecialEntity("aelig",	230, null, true));
		_put(new SpecialEntity("ccedil",	231, null, true));
		_put(new SpecialEntity("egrave",	232, null, true));
		_put(new SpecialEntity("eacute",	233, null, true));
		_put(new SpecialEntity("ecirc",	234, null, true));
		_put(new SpecialEntity("euml",	235, null, true));
		_put(new SpecialEntity("igrave",	236, null, true));
		_put(new SpecialEntity("iacute",	237, null, true));
		_put(new SpecialEntity("icirc",	238, null, true));
		_put(new SpecialEntity("iuml",	239, null, true));
		_put(new SpecialEntity("eth",		240, null, true));
		_put(new SpecialEntity("ntilde",	241, null, true));
		_put(new SpecialEntity("ograve",	242, null, true));
		_put(new SpecialEntity("oacute",	243, null, true));
		_put(new SpecialEntity("ocirc",	244, null, true));
		_put(new SpecialEntity("otilde",	245, null, true));
		_put(new SpecialEntity("ouml",	246, null, true));
		_put(new SpecialEntity("divide",	247, null, true));
		_put(new SpecialEntity("oslash",	248, null, true));
		_put(new SpecialEntity("ugrave",	249, null, true));
		_put(new SpecialEntity("uacute",	250, null, true));
		_put(new SpecialEntity("ucirc",	251, null, true));
		_put(new SpecialEntity("uuml",	252, null, true));
		_put(new SpecialEntity("yacute",	253, null, true));
		_put(new SpecialEntity("thorn",	254, null, true));
		_put(new SpecialEntity("yuml",	255, null, true));

		_put(new SpecialEntity("OElig",	338, null, true));
		_put(new SpecialEntity("oelig",	339, null, true));
		_put(new SpecialEntity("Scaron",	352, null, true));
		_put(new SpecialEntity("scaron",	353, null, true));
		_put(new SpecialEntity("Yuml",	376, null, true));
		_put(new SpecialEntity("circ",	710, null, true));
		_put(new SpecialEntity("tilde",	732, null, true));
		_put(new SpecialEntity("ensp",	8194, null, true));
		_put(new SpecialEntity("emsp",	8195, null, true));
		_put(new SpecialEntity("thinsp",	8201, null, true));
		_put(new SpecialEntity("zwnj",	8204, null, true));
		_put(new SpecialEntity("zwj",		8205, null, true));
		_put(new SpecialEntity("lrm",		8206, null, true));
		_put(new SpecialEntity("rlm",		8207, null, true));
		_put(new SpecialEntity("ndash",	8211, null, true));
		_put(new SpecialEntity("mdash",	8212, null, true));
		_put(new SpecialEntity("lsquo",	8216, null, true));
		_put(new SpecialEntity("rsquo",	8217, null, true));
		_put(new SpecialEntity("sbquo",	8218, null, true));
		_put(new SpecialEntity("ldquo",	8220, null, true));
		_put(new SpecialEntity("rdquo",	8221, null, true));
		_put(new SpecialEntity("bdquo",	8222, null, true));
		_put(new SpecialEntity("dagger",	8224, null, true));
		_put(new SpecialEntity("Dagger",	8225, null, true));
		_put(new SpecialEntity("hellip",	8230, null, true));
		_put(new SpecialEntity("permil",	8240, null, true));
		_put(new SpecialEntity("lsaquo",	8249, null, true));
		_put(new SpecialEntity("rsaquo",	8250, null, true));
        _put(new SpecialEntity("euro",  8364, null, true));
        _put(new SpecialEntity("trade",	8482, null, true));

        _put(new SpecialEntity("amp",  '&', null, false));
        _put(new SpecialEntity("lt", '<', null, false));
        _put(new SpecialEntity("gt",  '>', null, false));
        _put(new SpecialEntity("quot",  '"', null, false));
        // this is xml only -- apos appearing in html needs to be converted to ' or maybe &#39; to be universally safe
        _put(new SpecialEntity("apos",  '\'', "&#39;", false));
}

	/**
	 *
	 * @param seq expected to have a ';'
	 * @return {@link SpecialEntity} if found.
	 */
	public SpecialEntity getSpecialEntity(String seq) {
	    int startIndex = seq.charAt(0) == '&'?1:0;
        int semiIndex = seq.indexOf(';');
        SpecialEntity specialEntity = null;
        if (semiIndex > 0) {
            String entity = seq.substring(startIndex, semiIndex);
            specialEntity  = (SpecialEntity)entities.get(entity);
        }
	    return specialEntity;
	}
	
	public SpecialEntity getSpecialEntityByUnicode(int unicodeCharcode) {
	    return (SpecialEntity) this.entitiesByUnicodeCharcode.get(unicodeCharcode);
	}

	public void put(SpecialEntity specialEntity) {
	    _put(specialEntity);
	}

    /**
     * @param specialEntity
     */
    private void _put(SpecialEntity specialEntity) {
        entities.put(specialEntity.getKey(), specialEntity);
        entitiesByUnicodeCharcode.put(specialEntity.intValue(), specialEntity);
    }
}