package zen.zasm;

class StmtFwdTo extends Statement {
	private Token addr;

	public StmtFwdTo(Token tk, Tokenizer tz) {
		super(null, tk);
		addr = tz.nextToken();
	}

	public void compile(Toc toc) throws Throwable {
		int pc_nxt = Statement.parseUnsignedImm16(addr);
		toc.fwdPcTo(addr, pc_nxt, false);
	}

	public String toSource() {
		return super.toSource() + addr.toSource();
	}
}