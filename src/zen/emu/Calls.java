package zen.emu;

public final class Calls {
	final private int[] mem;
	int idx = -1;

	public Calls(final int addr_width) {
		mem = new int[2 ^ addr_width];
	}

	public void push(final int addr, final boolean zf, final boolean nf) {
		idx++;
		mem[idx] = addr | (zf ? 0x1000 : 0) | (nf ? 0x2000 : 0);
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
}
