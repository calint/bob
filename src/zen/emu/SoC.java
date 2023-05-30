package zen.emu;

public final class SoC {
	private final UartTx utx = new UartTx();
	private final UartRx urx = new UartRx();
	private final short[] ram = new short[(int) Math.pow(2, 16)];
	private Core core = new Core(4, 6, ram, utx, urx);

	public short[] getRAM() {
		return ram;
	}

	public void tick() {
		utx.tick();
		urx.tick();
		core.tick();
	}

	public Core core() {
		return core;
	}

	public void reset() {
		core.reset();
	}
}
