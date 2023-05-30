package zen.emu;


public final class UartTx {
	public int dataImm8;
	public boolean go;

	public void send(int imm8) {
		System.out.println("UartTx: " + imm8);
		dataImm8 = imm8;
		go = true;
	}

}
