package zen.zasm;

final class OpCp extends Statement {

    private final Token rega;
    private final Token regb;

    public OpCp(Token zn, Token tk, Tokenizer tz) {
        super(zn, tk);
        rega = tz.nextToken();
        regb = tz.nextToken();
        readOptionalRet(tz);
    }

    @Override
    public String toSource() {
        return super.toSource() + rega.toSource() + regb.toSource() + toSourceRet();
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        short instr = getZnr();
        short a = registerAddressFromToken(rega);
        short b = registerAddressFromToken(regb);
        instr |= 0xc << 4;
        instr |= a << 8;
        instr |= b << 12;
        toc.write(this, instr);
    }

}