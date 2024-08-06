package zen.zasm;

final class OpRl extends Statement {

    private final Token regb;

    public OpRl(Token zn, Token tk, Tokenizer tz) {
        super(zn, tk);
        regb = tz.nextToken();
        readOptionalRet(tz);
    }

    @Override
    public String toSource() {
        return super.toSource() + regb.toSource() + toSourceRet();
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        short instr = getZnr();
        short b = registerAddressFromToken(regb);
        instr |= 3 << 4;
        instr |= 6 << 8;
        instr |= b << 12;
        toc.write(this, instr);
    }

}