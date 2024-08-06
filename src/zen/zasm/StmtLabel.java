package zen.zasm;

final class StmtLabel extends Statement {

    private Token func;

    public StmtLabel(Token nm, Tokenizer tz) {
        super(null, nm);
        Token tp = tz.nextToken();
        if (tp.isId("func")) {
            func = tp;
        } else {
            tz.pushBackToken(tp);
        }
    }

    @Override
    public String toSource() {
        return super.toSource() + (func != null ? func.toSource() : "");
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        final String id = id();
        toc.addLabel(token(), id.substring(0, id.length() - 1), func != null);
    }

}