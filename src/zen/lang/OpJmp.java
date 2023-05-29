package zen.lang;

class OpJmp extends Statement {
	private Token lbl;

	public OpJmp(Token zn, Token tk, Tokenizer tz) {
		super(zn, tk);
		lbl = tz.nextToken();
	}

	public void compile(Toc toc) throws Throwable {
		short instr = getZnr();
		instr |= 0xc;
		toc.write(this, instr, Toc.LinkType.JMP, lbl.id(), lbl);
	}

	public String toSource() {
		return super.toSource() + lbl.toSource();
	}
}