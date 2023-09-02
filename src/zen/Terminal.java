package zen;

import b.a;
import b.xwriter;

public class Terminal extends a {
    private static final long serialVersionUID = 1L;

	@Override
    public void to(xwriter x) throws Throwable {
    }

    public void onKey(xwriter x, int key) {
        x.xp(this, "" + (char) key);
    }
}
