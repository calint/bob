package zen.lang;

class OpLd extends Statement {
	private Token rega;
	private Token regb;

	public OpLd(Token zn, Token tk, Tokenizer tz) {
		super(zn, tk);
		rega = tz.nextToken();
		regb = tz.nextToken();
		readOptionalRet(tz);
	}

	public String toSource() {
		return super.toSource() + rega.toSource() + regb.toSource() + toSourceRet();
	}

	public void compile(Toc toc) throws Throwable {
		short instr = getZnr();
		short a = registerAddressFromToken(rega);
		short b = registerAddressFromToken(regb);
		instr |= 5 << 4;
		instr |= a << 8;
		instr |= b << 12;
		toc.write(this, instr);
	}
}