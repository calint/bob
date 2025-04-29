//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.sql.Timestamp;

import b.a;
import b.xwriter;
import bob.Form;
import bob.Util;
import db.Db;
import db.DbTransaction;
import db.test.Book;
import db.test.DataText;

/** Example of use of bob abstract form. */
public final class FormBook extends Form {

    private final static long serialVersionUID = 1;

    public a title;
    public a authorsStr;
    public a publisherStr;
    public a publishedDate;

    public FormBook(final String objectId, final String initStr) {
        super(null, objectId, initStr, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);

        final Book o = (Book) (objectId == null ? null : Db.currentTransaction().get(Book.class, objectId));

        title.set(o == null ? initStr : o.getName());
        authorsStr.set(o == null ? "" : o.getAuthorsStr());
        publisherStr.set(o == null ? "" : o.getPublisherStr());
        publishedDate.set(o == null ? "" : Util.toStr(o.getPublishedDate(), ""));
    }

    public String title() {
        return Util.toStr(title.str(), "New book");
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        // note: postback to parent `x_sc` which is "save & close"
        x.p("title: ").inptxt(title, this, "sc").nl();
        x.focus(title);
        x.p("authors: ").inptxt(authorsStr, this, "sc").nl();
        x.p("publisher: ").inptxt(publisherStr, this, "sc").nl();
        x.p("publishedDate: ").inptxt(publishedDate, this, "sc").nl();
    }

    @Override
    protected void save(final xwriter x) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();

        final Book o;
        final String oid = getObjectId();
        if (oid == null) {
            o = (Book) tn.create(Book.class);
            setObjectId(Integer.toString(o.id()));
        } else {
            o = (Book) tn.get(Book.class, oid);
        }

        o.setName(title.str());
        o.setAuthorsStr(authorsStr.str());
        o.setPublisherStr(publisherStr.str());
        if (!publishedDate.is_empty()) {
            o.setPublishedDate(Timestamp.valueOf(publishedDate.str()));
        }
        final DataText d = o.getData(true);
        d.setMeta(o.getName() + " " + o.getAuthorsStr() + " " + o.getPublisher());
    }

}
