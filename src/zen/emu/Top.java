package zen.emu;

public final class Top {
	private final RAM ram = new RAM(16);
	private final UartTx utx = new UartTx();
	private final UartRx urx = new UartRx();

	private Core core = new Core(4, 6, ram, utx, urx);

	public void tick() {
		core.tick();
	}
}
