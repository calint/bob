// reviewed: 2024-08-05
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
    private static final long serialVersionUID = 1;

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

    public String getTitle() {
        return Util.toStr(title.str(), "New book");
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        x.p("title: ").inptxt(title, this, "sc").nl();
        x.script().xfocus(title).script_();
        x.p("authors: ").inptxt(authorsStr, this, "sc").nl();
        x.p("publisher: ").inptxt(publisherStr, this, "sc").nl();
        x.p("publishedDate: ").inptxt(publishedDate, this, "sc").nl();
    }

    @Override
    protected void save(final xwriter x) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        final Book o;
        if (objectId == null) {
            // create new
            o = (Book) tn.create(Book.class);
            objectId = Integer.toString(o.id());
        } else {
            o = (Book) tn.get(Book.class, objectId);
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
