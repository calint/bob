package zen.emu;

public final class SoC {
	private final UartTx utx = new UartTx();
	private final UartRx urx = new UartRx();
	public final short[] ram = new short[(int) Math.pow(2, 16)];
	public final Core core = new Core(4, 6, ram, utx, urx);

	public void tick() {
		utx.tick();
		urx.tick();
		core.tick();
	}

	public void reset() {
		core.reset();
	}
}
