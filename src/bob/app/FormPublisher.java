//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.DbObject;
import db.test.Publisher;

public final class FormPublisher extends FormDbo {

    private final static long serialVersionUID = 1;

    public FormPublisher() {
        this(null, null);
    }

    public FormPublisher(final String objectId, final String initStr) {
        super(null, Publisher.class, objectId, initStr);
    }

    public String title() {
        final Publisher o = (Publisher) object();
        return o == null ? "New publisher" : o.getName();
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final Publisher o = (Publisher) object();
        beginForm(x);
        inputText(x, "Name", o, Publisher.name, initString(), "medium");
        focus(x, Publisher.name);
        endForm(x);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        // `FormDbo` writes the input fields to the `DbObject`
    }
}
