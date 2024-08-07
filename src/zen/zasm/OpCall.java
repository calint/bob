package zen.zasm;

final class OpCall extends Statement {

    private final Token lbl;

    public OpCall(Token zn, Token tk, Tokenizer tz) {
        super(zn, tk);
        lbl = tz.nextToken();
    }

    @Override
    public String toSource() {
        return super.toSource() + lbl.toSource();
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        short instr = getZnr();
        instr |= 0x8;
        toc.write(this, instr, Toc.LinkType.CALL, lbl.id(), lbl);
    }

}