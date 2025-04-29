//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.util.ArrayList;
import java.util.List;

import b.xwriter;
import bob.Action;
import bob.FormDbo;
import bob.Util;
import db.DbObject;
import db.test.Author;
import db.test.Book;
import db.test.Category;
import db.test.DataText;
import db.test.Publisher;
import db.test.User;

/** Example using abstract `DbObject` edit form. */
public final class FormBook2 extends FormDbo {

    private final static long serialVersionUID = 1;

    public FormBook2() {
        this(null, null);
    }

    public FormBook2(final String objectId, final String initStr) {
        super(null, Book.class, objectId, initStr);
    }

    public String getTitle() {
        final Book o = (Book) getObject();
        return o == null ? "New book" : o.getName();
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final Book o = (Book) getObject();
        beginForm(x);
        inputText(x, "Title", o, Book.name, getInitStr(), "long");
        focus(x, Book.name);
        inputRefN(x, "Authors", o, Book.authors, null, TableAuthors.class, FormAuthor.class);
        inputRef(x, "Publisher", o, Book.publisher, 0, TablePublishers.class, FormPublisher.class);
        inputDate(x, "Published date", o, Book.publishedDate, null);
        inputRefN(x, "Categories", o, Book.categories, null, TableCategories.class, FormCategory.class);
        inputInt(x, "In stock", o, Book.inStock, 0, "nbr");
        inputBool(x, "Show in store", o, Book.showInStore, true);
        inputFlt(x, "Rating", o, Book.rating, 0, "nbr");
        // aggregated object field in form field `description`
        inputTextArea(x, "Description", "description", o == null ? "" : o.getData(true).getData(), "large");
        endForm(x);
        x.ax(this, "rfsh", "refresh").nl();
    }

    /** Additional processing at `save`. */
    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        final Book o = (Book) obj;

        if (Util.isEmpty(o.getName())) {
            xfocus(x, User.name);
            throw new Exception("Title may not be empty.");
        }

        // denormalize for better performance
        final StringBuilder authorsSb = new StringBuilder(128);
        for (final DbObject ao : o.getAuthors().toList()) {
            final Author a = (Author) ao;
            authorsSb.append(a.getName()).append(';');
        }
        if (authorsSb.length() > 1) {
            authorsSb.setLength(authorsSb.length() - 1);
        }
        o.setAuthorsStr(authorsSb.toString());

        // denormalize for better performance
        final Publisher publisher = o.getPublisher();
        o.setPublisherStr(publisher == null ? "" : publisher.getName());

        // denormalize for better performance
        final StringBuilder categoriesSb = new StringBuilder(128);
        for (final DbObject co : o.getCategories().toList()) {
            final Category c = (Category) co;
            categoriesSb.append(c.getName()).append(';');
        }
        if (categoriesSb.length() > 1) {
            categoriesSb.setLength(categoriesSb.length() - 1);
        }
        o.setCategoriesStr(categoriesSb.toString());

        // aggregated object
        final DataText d = o.getData(true);
        d.setMeta(o.getName() + " " + o.getAuthorsStr() + " " + o.getPublisherStr() + " " + o.getCategoriesStr());
        d.setData(getStr("description"));
    }

    /** Additional actions for this form. */
    @Override
    protected List<Action> getActionsList() {
        final List<Action> ls = new ArrayList<Action>();
        ls.add(new Action("alert me", "alert")); // sample action
        return ls;
    }

    @Override
    protected void onAction(final xwriter x, final Action act) throws Throwable {
        if ("alert".equals(act.code())) {
            // handle action `alert`
            x.xalert("alert");
            return;
        }
        super.onAction(x, act);
    }

    public void x_rfsh(final xwriter x, final String param) throws Throwable {
        x.xu(this);
    }

}
