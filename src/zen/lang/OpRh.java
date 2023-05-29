package zen.lang;

class OpRh extends Statement {
	private Token regb;

	public OpRh(Token zn, Token tk, Tokenizer tz) {
		super(zn, tk);
		regb = tz.nextToken();
		readOptionalRet(tz);
	}

	public void compile(Toc toc) throws Throwable {
		short instr = getZnr();
		short b = registerAddressFromToken(regb);
		instr |= 3 << 4;
		instr |= 0xe << 8;
		instr |= b << 12;
		toc.write(this, instr);
	}

	public String toSource() {
		return super.toSource() + regb.toSource() + toSourceRet();
	}
}