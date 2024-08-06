package zen.zasm;

final class OpLedi extends Statement {

    private final Token regb;

    public OpLedi(Token zn, Token tk, Tokenizer tz) {
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
        short b = parseImm4(regb);
        instr |= 3 << 4;
        instr |= 0xf << 8;
        instr |= b << 12;
        toc.write(this, instr);
    }

}