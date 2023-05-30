package zen;

import b.a;
import b.xwriter;

public class Terminal extends a {
    @Override
    public void to(xwriter x) throws Throwable {
        x.divo(this, "term", null).tagoe().div_();
    }

    public void onKey(xwriter x, int key) {
        x.xp(this, "" + (char) key);
    }
}
