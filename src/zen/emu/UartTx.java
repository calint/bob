//
// reviewed: 2024-04-29
//
package zen.emu;

public final class UartTx {

    public int data;
    public boolean go;

    public void send(int imm8) {
        data = imm8;
        go = true;
    }

}
