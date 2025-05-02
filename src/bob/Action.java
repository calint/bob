//
// reviewed: 2024-08-05
//           2025-04-28
//           2025-05-02
//
package bob;

import b.a;
import b.xwriter;

/** Represents an action containing a code. */
public class Action extends a {

    private final static long serialVersionUID = 1;

    private final String code;

    public Action(final String text, final String code) {
        set(text);
        this.code = code;
    }

    public final String code() {
        return code;
    }

    @Override
    public final void to(final xwriter x) throws Throwable {
        // default callback for click
        x.ax(this, null, str(), "act");
    }

    /** Default callback. */
    public final void x_(final xwriter x, final String param) throws Throwable {
        super.bubble_event(x);
    }

}
