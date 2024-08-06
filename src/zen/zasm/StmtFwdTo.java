package zen.zasm;

final class StmtFwdTo extends Statement {

    private final Token addr;

    public StmtFwdTo(Token tk, Tokenizer tz) {
        super(null, tk);
        addr = tz.nextToken();
    }

    @Override
    public String toSource() {
        return super.toSource() + addr.toSource();
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        int pc_nxt = Statement.parseUnsignedImm16(addr);
        toc.fwdPcTo(addr, pc_nxt, false);
    }

}