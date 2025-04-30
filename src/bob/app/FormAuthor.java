//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.DbObject;
import db.test.Author;

public final class FormAuthor extends FormDbo {

    private final static long serialVersionUID = 1;

    public FormAuthor() {
        this(null, null);
    }

    public FormAuthor(final String objectId, final String initStr) {
        super(null, Author.class, objectId, initStr);
    }

    public String title() {
        final Author o = (Author) object();
        return o == null ? "New author" : o.getName();
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final Author o = (Author) object();
        beginForm(x);
        inputText(x, "Name", o, Author.name, initString(), "medium");
        focus(x, Author.name);
        endForm(x);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        final Author o = (Author) obj;
        if (o.getName().length() == 0) {
            xfocus(x, Author.name);
            throw new Exception("Name must be specified.");
        }
    }

}
