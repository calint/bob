package zen.emu;

public final class SoC {
	public final UartTx utx = new UartTx();
	public final UartRx urx = new UartRx();
	public final short[] ram = new short[65536];
	public final Core core = new Core(4, 6, ram, utx, urx);

	public void tick() {
		core.tick();
	}

	public void reset() {
		core.reset();
		for (int i = 0; i < ram.length; i++) {
			ram[i] = 0;
		}
	}
}
