// reviewed: 2024-08-05
package bob.app;

import b.a;
import b.a_ajaxsts;
import b.xwriter;

/**
 * Postback based user interface. Saves the state in `db`.
 */
public final class ControllerPostback extends a {

    private final static long serialVersionUID = 1;

    public Ctrl c; // controller
    public a_ajaxsts ajaxsts; // ajax status, must be root element named `ajaxsts`

    @Override
    public void to(final xwriter x) throws Throwable {
        x.pl("<title>bob</title>");
        x.pl("<link rel=stylesheet href=/bob.css>");
        ajaxsts.to(x);
        x.nl();
        x.divh(c, "c").nl(); // canvas of root element
    }

}
