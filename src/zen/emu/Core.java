package zen.emu;

public final class Core {

    private final UartTx utx;
    private final UartRx urx;

    public final short[] regs;
    public final Calls cs;
    public final short[] ram;

    public long tick;
    public int pc;
    public boolean zf;
    public boolean nf;
    public int leds;

    private static final int OP_ADDI = 0x1;
    private static final int OP_LDI = 0x3;
    private static final int OP_LD = 0x5;
    private static final int OP_ST = 0x7;

    private static final int OP_ADD = 0x0;
    private static final int OP_SUB = 0x2;
    private static final int OP_OR = 0x4;
    private static final int OP_XOR = 0x6;
    private static final int OP_AND = 0x8;
    private static final int OP_NOT = 0xa;
    private static final int OP_CP = 0xc;
    private static final int OP_SHF = 0xe;

    private static final int OP_IO_WL = 0x2;
    private static final int OP_IO_WH = 0xa;
    private static final int OP_IO_RL = 0x6;
    private static final int OP_IO_RH = 0xe;
    private static final int OP_IO_LED = 0x7;
    private static final int OP_IO_LEDI = 0xf;

    public Core(final int regs_addr_width, final int calls_addr_width, final short[] ram, final UartTx utx,
            final UartRx urx) {
        regs = new short[(int) Math.pow(2, regs_addr_width)];
        cs = new Calls(calls_addr_width);
        this.ram = ram;
        this.utx = utx;
        this.urx = urx;
    }

    public void reset() {
        tick = 0;
        pc = 0;
        zf = false;
        nf = false;
        leds = 0;
        cs.reset();
        for (int i = 0; i < regs.length; i++) {
            regs[i] = 0;
        }
    }

    public void tick() { // ? does not emulate pipeline bubble or 2 cycle ldi
        tick++;

        final short instr = ram[pc];
        final boolean instr_z = (instr & 1) != 0;
        final boolean instr_n = (instr & 2) != 0;
        final boolean instr_r = (instr & 4) != 0;
        final boolean instr_c = (instr & 8) != 0;
        final int op = (instr & 0x00f0) >> 4;
        final int rega = (instr & 0x0f00) >> 8;
        final int regb = (instr & 0xf000) >> 12;
        final int imm12 = (instr & 0xfff0) >> 4;

        final boolean is_do_op = ((instr_z && instr_n) || (instr_z == zf && instr_n == nf));

        if (!is_do_op) {
            pc++;
            // check that is not a 'jmp' or 'call' and 'ldi'
            if (!(instr_c && instr_r) && !(instr_c && !instr_r) && op == OP_LDI && rega == 0) {
                // one more step for 'ldi'
                pc++;
                tick++;
            }
            return;
        }

        if (is_do_op && instr_c && !instr_r) { // call
            cs.push(pc, zf, nf);
            pc = imm12 << 4;
            zf = false;
            nf = false;
            tick++; // bubble
            return;
        }

        if (is_do_op && instr_c && instr_r) { // jmp
            final int signedImm12 = signedImm12ToInt(imm12);
            pc += signedImm12;
            tick++; // bubble
            return;
        }

        boolean is_stall = false;

        if (op == OP_LDI && rega != 0) { // is I/O ?
            switch (rega) {
            case OP_IO_WL:
                utx.send(regs[regb] & 0x00ff);
                break;
            case OP_IO_WH:
                utx.send(((regs[regb] & 0xff00) >> 8) & 0xff);
                break;
            case OP_IO_RL:
                if (urx.dr) {
                    regs[regb] = (short) ((regs[regb] & 0xff00) | urx.data);
                    urx.dr = false;
                } else {
                    is_stall = true;
                }
                break;
            case OP_IO_RH:
                if (urx.dr) {
                    regs[regb] = (short) ((regs[regb] & 0x00ff) | urx.data << 8);
                    urx.dr = false;
                } else {
                    is_stall = true;
                }
                break;
            case OP_IO_LED:
                leds = regs[regb] & 0x000f;
                break;
            case OP_IO_LEDI:
                leds = regb;
                break;
            }
        } else {
            short res;
            switch (op) {
            case OP_ADD:
                res = (short) (regs[regb] + regs[rega]);
                zn(res);
                regs[regb] = res;
                break;
            case OP_SUB:
                res = (short) (regs[regb] - regs[rega]);
                zn(res);
                regs[regb] = res;
                break;
            case OP_OR:
                res = (short) (regs[regb] | regs[rega]);
                zn(res);
                regs[regb] = res;
                break;
            case OP_XOR:
                res = (short) (regs[regb] ^ regs[rega]);
                zn(res);
                regs[regb] = res;
                break;
            case OP_AND:
                res = (short) (regs[regb] & regs[rega]);
                zn(res);
                regs[regb] = res;
                break;
            case OP_NOT:
                res = (short) ~regs[rega];
                zn(res);
                regs[regb] = res;
                break;
            case OP_CP:
                res = regs[rega];
                zn(res);
                regs[regb] = res;
                break;
            case OP_SHF:
                final int nbits = signedImm4PosIncInt(rega);
                res = (short) (nbits < 0 ? regs[regb] << nbits : regs[regb] >> nbits);
                zn(res);
                regs[regb] = res;
                break;
            case OP_ADDI:
                res = (short) (regs[regb] + signedImm4PosIncInt(rega));
                zn(res);
                regs[regb] = res;
                break;
            case OP_LDI:
                pc++;
                regs[regb] = ram[pc];
                tick++; // skipped cycle
                break;
            case OP_LD: {
                regs[regb] = ram[shortToUnsignedInt(regs[rega])];
                break;
            }
            case OP_ST: {
                ram[shortToUnsignedInt(regs[rega])] = regs[regb];
                break;
            }
            default:
                assert (false);
            }
        }

        if (is_stall) {
            return;
        }

        if (is_do_op && !instr_c && instr_r) { // ret
            zf = cs.getZf();
            nf = cs.getNf();
            pc = cs.getPc() + 1;
            cs.pop();
            tick++; // bubble
        } else {
            pc++;
        }

    }

    private void zn(final short i) {
        zf = i == 0;
        nf = i < 0;
    }

    private static int signedImm12ToInt(final int imm12) {
        return ((imm12 & 0x800) != 0) ? (imm12 | 0xfffff000) : imm12;
    }

    private static int shortToUnsignedInt(final short i) {
        return i < 0 ? i + 32768 : i;
    }

    private static int signedImm4PosIncInt(final int imm4) {
        if ((imm4 & 0x8) != 0) {
            return imm4 | 0xfffffff0; // sign extend
        }
        return imm4 + 1;
    }

    public static final class Calls {
        public final int[] mem;
        public int idx = -1;

        public Calls(final int addr_width) {
            mem = new int[(int) Math.pow(2, addr_width)];
        }

        public void push(final int addr, final boolean zf, final boolean nf) {
            idx++;
            mem[idx] = addr | (zf ? 0x10000 : 0) | (nf ? 0x20000 : 0);
        }

        public void pop() {
            idx--;
            assert (idx >= -1);
        }

        public int getPc() {
            return mem[idx] & 0xffff;
        }

        public boolean getZf() {
            return (mem[idx] & 0x10000) != 0;
        }

        public boolean getNf() {
            return (mem[idx] & 0x20000) != 0;
        }

        public void reset() {
            for (int i = 0; i < mem.length; i++) {
                mem[i] = 0;
                idx = -1;
            }
        }
    }

}
