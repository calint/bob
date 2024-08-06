package zen.zasm;

final class StmtData extends Statement {

    public StmtData(Token data) {
        super(null, data);
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        toc.write(this, parseImm16(token()));
    }

}