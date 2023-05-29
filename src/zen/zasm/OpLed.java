package zen.zasm;

class OpLed extends Statement {
	private Token regb;

	public OpLed(Token zn, Token tk, Tokenizer tz) {
		super(zn, tk);
		regb = tz.nextToken();
		readOptionalRet(tz);
	}

	public void compile(Toc toc) throws Throwable {
		short instr = getZnr();
		short b = registerAddressFromToken(regb);
		instr |= 3 << 4;
		instr |= 7 << 8;
		instr |= b << 12;
		toc.write(this, instr);
	}

	public String toSource() {
		return super.toSource() + regb.toSource() + toSourceRet();
	}
}