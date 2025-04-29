//
// reviewed: 2025-04-29
//
package zen;

import b.a;
import b.xwriter;

public class LineNums extends a {

    private final static long serialVersionUID = 1;

    public static int LINE_NUMS = 256;

    @Override
    public void to(final xwriter x) throws Throwable {
        for (int i = 1; i <= LINE_NUMS; i++) {
            x.p(i).br();
        }
    }

}
