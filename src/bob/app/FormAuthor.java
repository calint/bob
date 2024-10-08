// reviewed: 2024-08-05
package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.DbObject;
import db.test.Author;

public final class FormAuthor extends FormDbo {
    private static final long serialVersionUID = 1;

    public FormAuthor() {
        this(null, null);
    }

    public FormAuthor(final String objectId, final String initStr) {
        super(null, Author.class, objectId, initStr);
    }

    public String getTitle() {
        final Author o = (Author) getObject();
        return o == null ? "New author" : o.getName();
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final Author o = (Author) getObject();
        beginForm(x);
        inputText(x, "Name", o, Author.name, getInitStr(), "medium");
        focus(x, Author.name);
        endForm(x);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
    }
}
