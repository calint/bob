package zen.lang;

class StmtEndFunc extends Statement {
	public StmtEndFunc(Token tk) {
		super(null, tk);
	}

	public void compile(Toc toc) throws Throwable {
		toc.exitFunc(token());
	}
}