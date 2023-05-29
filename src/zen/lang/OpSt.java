package zen.lang;

class OpSt extends Statement {
	private Token rega;
	private Token regb;

	public OpSt(Token zn, Token tk, Tokenizer tz) {
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
		instr |= 7 << 4;
		instr |= a << 8;
		instr |= b << 12;
		toc.write(this, instr);
	}
}