package zen.zasm;

final class StmtEndFunc extends Statement {

    public StmtEndFunc(Token tk) {
        super(null, tk);
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        toc.exitFunc(token());
    }

}