// reviewed: 2024-08-05
package bob;

import b.a;
import b.xwriter;

public class Action extends a {
    private static final long serialVersionUID = 1;

    private final String code;

    public Action() {
        this("", "");
    }

    public Action(final String text, final String code) {
        set(text);
        this.code = code;
    }

    public final String code() {
        return code;
    }

    @Override
    public final void to(final xwriter x) throws Throwable {
        x.ax(this, null, str());
    }

    public final void x_(final xwriter x, final String param) throws Throwable {
        super.bubble_event(x);
    }

}
