package zen.zasm;

class StmtData extends Statement {

	public StmtData(Token data) {
		super(null, data);
	}

	public void compile(Toc toc) throws Throwable {
		toc.write(this, parseImm16(token()));
	}
}