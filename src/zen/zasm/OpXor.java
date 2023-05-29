package zen.zasm;

class OpXor extends Statement {
	private Token rega;
	private Token regb;

	public OpXor(Token zn, Token tk, Tokenizer tz) {
		super(zn, tk);
		rega = tz.nextToken();
		regb = tz.nextToken();
		readOptionalRet(tz);
	}

	public void compile(Toc toc) throws Throwable {
		short instr = getZnr();
		short a = registerAddressFromToken(rega);
		short b = registerAddressFromToken(regb);
		instr |= 6 << 4;
		instr |= a << 8;
		instr |= b << 12;
		toc.write(this, instr);
	}

	public String toSource() {
		return super.toSource() + rega.toSource() + regb.toSource() + toSourceRet();
	}
}