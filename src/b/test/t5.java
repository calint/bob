package b.test;

import b.a;
import b.xwriter;

/** test form */
public class t5 extends a {
    private static final long serialVersionUID = 3;

    public a fld;

    @Override
    public void to(final xwriter x) throws Throwable {
        x.inptxt(fld).br().ax(this, "send", "send");
    }

    public void x_send(final xwriter x, final String param) throws Throwable {
        x.xalert(fld.str());
    }
}
