package zen.zasm;

final class OpLdi extends Statement {

    private final Token regb;
    private final Token imm16;

    public OpLdi(Token zn, Token tk, Tokenizer tz) {
        super(zn, tk);
        imm16 = tz.nextToken();
        regb = tz.nextToken();
        readOptionalRet(tz);
    }

    @Override
    public String toSource() {
        return super.toSource() + imm16.toSource() + regb.toSource() + toSourceRet();
    }

    @Override
    public void compile(Toc toc) throws Throwable {
        short instr = getZnr();
        short reg = registerAddressFromToken(regb);
        instr |= 3 << 4;
        instr |= reg << 12;
        toc.write(this, instr);
        try {
            short addr = Statement.parseImm16(imm16);
            toc.write(this, addr);
        } catch (Throwable t) {
            toc.write(this, (short) 0, Toc.LinkType.LDI, imm16.id(), imm16);
        }
    }

}