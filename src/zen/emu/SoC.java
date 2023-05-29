package zen.emu;

public final class SoC {
	private final UartTx utx = new UartTx();
	private final UartRx urx = new UartRx();

	private Core core = new Core(4, 6, new short[2 ^ 16], utx, urx);

	public void tick() {
		utx.tick();
		urx.tick();
		core.tick();
	}
}
