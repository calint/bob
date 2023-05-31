package zen.zasm;

class Statement {
	private Token tk;
	private Token zn;
	private Token ret;

	public Statement(Token zn, Token tk) {
		this.zn = zn;
		this.tk = tk;
	}

	void readOptionalRet(Tokenizer tz) {
		Token tk = tz.nextToken();
		if (tk.isId("ret")) {
			ret = tk;
		} else {
			tz.pushBackToken(tk);
		}
	}

	public String toSource() {
		return (zn == null ? "" : zn.toSource()) + tk.toSource();
	}

	public String toSourceRet() {
		if (ret == null)
			return "";
		return ret.toSource();
	}

	public short getZnr() {
		short r = (short) (ret == null ? 0 : 4);
		if (zn != null) {
			if (zn.isId("ifz")) {
				return (short) (1 | r);
			}
			if (zn.isId("ifn")) {
				return (short) (2 | r);
			}
			if (zn.isId("ifp")) {
				return (short) (0 | r);
			}
		}
		return (short) (3 | r);
	}

	public void compile(Toc toc) throws Throwable {
	}

	public String id() {
		return tk.id();
	}

	public Token token() {
		return tk;
	}

	public String sourcePos() {
		return zn == null ? tk.sourcePos() : zn.sourcePos();
	}

	public int[] sourceRange() {
		return new int[] { zn != null ? zn.getStartPos() : tk.getStartPos(),
				ret != null ? ret.getEndPos() : tk.getEndPos() };
	}

	public static short registerAddressFromToken(Token addr) throws Throwable {
		if (!addr.id().startsWith("r"))
			throw new Exception(
				addr.sourcePos() + ": register '" + addr.id() + "' is not valid. valid registers 'r0' through 'r15'");

		short a = Short.parseShort(addr.id().substring(1));
		if (a < 0 || a > 15) {
			throw new Exception(
					addr.sourcePos() + ": unknown register '" + addr.id() + "'. valid registers are 'r0' through 'r15'");
		}
		return a;
	}

	public static short parseImm16(Token tk) throws Throwable {
		try {
			int i;
			if (tk.id().startsWith("0b")) {
				i = Integer.parseInt(tk.id().substring(2), 2);
			} else {
				i = Integer.decode(tk.id()).intValue();
			}
			if (i < 0 && i < Short.MIN_VALUE)
				throw new Exception(tk.sourcePos() + ": negative value '" + tk.id() + "' (" + i
						+ ") does not fit in 16 bits. min value is " + Short.MIN_VALUE);
			if (i > 0 && i > (1 << 16) - 1)
				throw new Exception(tk.sourcePos() + ": positive value '" + tk.id() + "' (" + i
						+ ") does not fit in 16 bits. max value is " + ((1 << 16) - 1));
			return (short) i;
		} catch (NumberFormatException e) {
			throw new Exception(tk.sourcePos() + ": cannot parse '" + tk.id() + "' to a number");
		}
	}

	public static int parseUnsignedImm16(Token tk) throws Throwable {
		try {
			int i;
			if (tk.id().startsWith("0b")) {
				i = Integer.parseInt(tk.id().substring(2), 2);
			} else {
				i = Integer.decode(tk.id()).intValue();
			}
			if (i < 0 || i > 0xffff)
				throw new Exception(tk.sourcePos() + ": unsigned 16 bit value '" + tk.id() + "' (" + i
						+ ") is not within range of 0x0000 and 0xffff");
			return i;
		} catch (NumberFormatException e) {
			throw new Exception(tk.sourcePos() + ": cannot parse '" + tk.id() + "' to a number");
		}
	}

	public static short parseImm4(Token tk) throws Throwable {
		int i;
		try {
			if (tk.id().startsWith("0b")) {
				i = Integer.parseInt(tk.id().substring(2), 2);
			} else {
				i = Integer.decode(tk.id()).intValue();
			}
		} catch (NumberFormatException e) {
			throw new Exception(tk.sourcePos() + ": cannot parse '" + tk.id() + "' to a number");
		}
		if (i > 15 || i < 0) {
			throw new Exception(tk.sourcePos() + ": immediate 4 bit value '" + i + "' is is out of 0 -> 15 range.");
		}
		return (short) i;
	}

	public static short parseSignedPosIncImm4(Token tk) throws Throwable {
		int a;
		try {
			a = Integer.decode(tk.id()).shortValue();
		} catch (NumberFormatException e) {
			throw new Exception(tk.sourcePos() + ": cannot parse '" + tk.id() + "' to a number");
		}
		if (a == 0)
			throw new Exception(
					tk.sourcePos() + ": signed immediate 4 bit value in this context is not allowed to be 0");

		if (a > 8 || a < -8) {
			throw new Exception(tk.sourcePos() + ": signed immediate 4 bit value '" + a
					+ "' is not within -8 to 8 (with 0 being invalid).");
		}
		return (short) a;
	}
}