package foldocparser;


/**
 * A Dictionary token, eventually kept with each link to
 * represent tags.<br>
 * When serialized, only the flags (a single integer) are kept.
 */
public class Token implements java.io.Serializable {

	/**
	 * versioning for serailsation
	 */
	private static final long serialVersionUID = 1L;

	//types
	public static final int
		M_TYPE = 0x1,
		H_LINK = 0x0,	 //either hyperlink or keyword
		K_WORD = 0x1,

		M_STRENGTH = 0x6,
		//increasing order of strength so that WEAK < NORMAL < STRONG < ...
		//so that double keywords can pick the strongest one
		WEAK = 0x0,
		NORMAL = 0x2,
		STRONG = 0x4,	 //flag: set if keyword is strong
		VERY_STRONG = 0x6,

		M_ASSOC = 0x38,
		CHILD = 0x0,
		PARENT = 0x8,	 //flag: set if keyword => parent of next h_link
		SIBLING = 0x10,
		DISSIM = 0x18,
		CATEGORY = 0x20,
		SYNONYM = 0x28,		// = (very) strong sibling
		UNKNOWN = 0x30,

		EOD = 0x40,	   //end of definition
		EOS = 0x80,	   //end of sentence
		EOP = 0x2000,	 //end of paragraph (ignored?)
		
		//for keywords
                //indicates there is also a keyword beginning with desc and a with different meaning
                M_KEYTYPE = 0x700,
		K_LONGER = 0x100 | K_WORD,	  //always | K_WORD
		//indicates this is not a keyword but there is one beginning with desc
		K_INVALID = 0x200 | K_WORD,
		K_NOTHING = 0x400,	  //no keyword - may be interesting though

		WEB_REF = 0x800,
		NULL_WORD = 0x1000 | H_LINK

	;

	public static final Token
			T_SYN  = new Token(K_WORD | VERY_STRONG | SYNONYM, ""),	//default synonym
			T_NORM = new Token(K_WORD | NORMAL | UNKNOWN, ""),		//default keyword property
			T_WEAKSYN = new Token(K_WORD | STRONG | CHILD, "");

	private int flags;
	private String desc;

	public Token(int flags, String text) {
		this.flags = flags;
		if (text.length() > 2 && text.charAt(0) == '-' && text.charAt(1) == ' ') {
			desc = "-" + text.substring(2);
		} else {
			desc = text;
		}
	}
	
	public String strengthString() {
		switch (flags & M_STRENGTH) {
		case WEAK: return "weak";
		case NORMAL: return "normal";
		case STRONG: return "strong";
		case VERY_STRONG: return "very strong";
		}
		return "unknown";
	}

	public String typeString() {
		switch (flags & M_ASSOC) {
		case CHILD: return "child";
		case PARENT: return "parent";
		case SIBLING: return "sibling";
		case DISSIM: return "antonym";
		case CATEGORY: return "category";
		case SYNONYM: return "synonym";
		}
		return "unknown";
	}

	public String toString() {
		return desc;// + Integer.toHexString(flags);
	}

	public int hashCode() {
		return desc.hashCode();
	}

	public boolean equals(Object o) {
		return desc.equals(o);
	}

	public int getFlags() {
		return flags;
        }

        public void setFlags(int flg) {
            flags = flg;
        }

	//base weight for keyword-added
	//fiddling with these will change the link weights
	public double baseWeight() {
		if ((flags & M_ASSOC) == CATEGORY)
			return 0.0;
		if ((flags & M_ASSOC) == SYNONYM && (flags & M_STRENGTH) == VERY_STRONG)
			return 0.01;
		switch (flags & M_STRENGTH) {
		case (WEAK):
			return 0.8;
		case (NORMAL):
			return 0.5;
		case (STRONG):
			return 0.3;
		case (VERY_STRONG):
			return 0.1;
		}
		return 0.5;
	}

	public boolean isChild() {
		return (flags & M_ASSOC) == CHILD;
	}

	public boolean isStrictParent() {
		return (flags & M_ASSOC) == PARENT;
	}

	public Token reverse() {
		Token r = duplicate();
		switch (r.flags & M_ASSOC) {
		case CHILD:
			r.flags = (r.flags & ~M_ASSOC) | PARENT;
			return r;
		case PARENT:
			r.flags = (r.flags & ~M_ASSOC) | CHILD;
		}
		return r;
	}

	//default to parent as natural linkage from definition body
	public boolean isParent() {
		int masked = flags & M_ASSOC;
		return masked == PARENT  ||
			   masked == SIBLING ||
			   masked == UNKNOWN ||
			   masked == DISSIM  ||
			   masked == CATEGORY ||
			   masked == SYNONYM;
	}

	public boolean isSynonym() {
		return (flags & M_ASSOC) == SYNONYM;
	}
	
	public boolean isAntonym() {
		return (flags & M_ASSOC) == DISSIM;
	}

	public boolean isBiDir() {
		int masked = flags & M_ASSOC;
		return masked == SYNONYM ||
			   (masked == SIBLING && ((flags & M_STRENGTH) >= STRONG));
	}

	public boolean isNoDir() {
		return (flags & M_ASSOC) == SIBLING || (flags & M_ASSOC) == UNKNOWN;
	}
	//etc

	public Token duplicate() {
		return new Token(flags, desc);
	}

	public void weaken() {
		switch (flags & M_STRENGTH) {
		case (WEAK):
			break;
		case (NORMAL):
			flags = (flags & ~M_STRENGTH) | WEAK;
			break;
		case (STRONG):
			flags = (flags & ~M_STRENGTH) | NORMAL;
			break;
		case (VERY_STRONG):
			flags = (flags & ~M_STRENGTH) | STRONG;
			break;
		}
	}
}
