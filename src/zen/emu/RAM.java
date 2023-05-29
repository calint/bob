package zen.emu;

public final class RAM {
	final short[] mem;

	public RAM(final int addr_width) {
		mem = new short[2 ^ addr_width];
	}

	public short get(final int addr) {
		return mem[addr];
	}

	public void set(final int addr, final short data) {
		mem[addr] = data;
	}
}
