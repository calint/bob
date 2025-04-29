//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.util.List;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.DbObject;
import db.test.File;

/**
 * Abstract form for creating/editing a `db.test.File` object. Implement
 * `createObject()` to create the file for the relevant object.
 */
public abstract class FormFileAbstract extends FormDbo {

    private final static long serialVersionUID = 1;

    public FormFileAbstract() {
        this(null, null, null);
    }

    public FormFileAbstract(final List<String> idPath, final String objectId, final String initStr) {
        super(idPath, File.class, objectId, initStr);
    }

    public String title() {
        return Util.toStr(getStr(File.name), "New file");
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final File o = (File) getObject();
        beginForm(x);
        inputText(x, "Name", o, File.name, getInitStr(), "medium");
        focus(x, File.name);
        endForm(x);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        // `FormDbo` writes the input fields to the `DbObject`
    }

    @Override
    protected abstract DbObject createObject();

    @Override
    protected abstract DbObject getObject();

}
