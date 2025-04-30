//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import b.a;
import b.xwriter;

public final class Checkbox extends a {

    private final static long serialVersionUID = 1;

    private final static String off = "0";
    private final static String on = "1";

    private final String value;

    public Checkbox(final String value, final boolean checked) {
        this.value = value;
        set(checked ? on : off);
    }

    public String value() {
        return value;
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        x.inp(this, "checkbox", null, null, str(), null, null, this, null);
    }

    public void x_(final xwriter x, final String param) throws Throwable {
        bubble_event(x, this, on.equals(str()) ? "checked" : "unchecked");
    }

}
