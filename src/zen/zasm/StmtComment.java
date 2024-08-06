package zen.zasm;

final class StmtComment extends Statement {

    private final String txt;

    public StmtComment(Token tk, Tokenizer tz) {
        super(null, tk);
        txt = tz.readComment();
    }

    @Override
    public String toSource() {
        return super.toSource() + txt;
    }
}