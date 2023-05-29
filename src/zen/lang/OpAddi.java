package zen.lang;

class OpAddi extends Statement {
	private Token imm4;
	private Token regb;

	public OpAddi(Token zn, Token tk, Tokenizer tz) {
		super(zn, tk);
		imm4 = tz.nextToken();
		regb = tz.nextToken();
		readOptionalRet(tz);
	}

	public String toSource() {
		return super.toSource() + imm4.toSource() + regb.toSource() + toSourceRet();
	}

	public void compile(Toc toc) throws Throwable {
		short instr = getZnr();
		short a = parseSignedPosIncImm4(imm4);
		short b = registerAddressFromToken(regb);
		if (a > 8 || a < -8) {
			throw new Exception(
					imm4.sourcePos() + ": immediate 4 bit value '" + a + "' may be -8 through 8 excluding 0.");
		}
		instr |= 1 << 4;
		if (a >= 0) {
			a--;
		}
		short imm4 = (short) (a >= 0 ? a : (((a & 0x07) - 8) & 0xf));
		instr |= imm4 << 8;
		instr |= b << 12;
		toc.write(this, instr);
	}
}