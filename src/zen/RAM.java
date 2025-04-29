//
// reviewed: 2025-04-29
//
package zen;

import b.a;
import b.xwriter;

public class RAM extends a {

    private final static long serialVersionUID = 1;

    public int base_addr;
    public short[] ram;

    @Override
    public void to(final xwriter x) throws Throwable {
        int i = 0;
        for (int s = 0; s < 16; s++) {
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    x.p(String.format("%04x", ram[base_addr + i] & 0xffff)).p(' ');
                    i++;
                }
                x.br();
            }
            x.br();
        }
    }

}
