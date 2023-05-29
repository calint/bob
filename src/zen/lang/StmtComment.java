package zen.lang;

class StmtComment extends Statement {
	private String txt;

	public StmtComment(Token tk, Tokenizer tz) {
		super(null, tk);
		txt = tz.readComment();
	}

	public String toSource() {
		return super.toSource() + txt;
	}
}