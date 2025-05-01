//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import b.a;
import b.xwriter;
import bob.Form;
import bob.FormDbo;
import bob.Util;
import bob.View;
import bob.ViewTable;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.Game;
import db.test.User;

public final class FormUser extends FormDbo {

    private final static long serialVersionUID = 1;

    /**
     * Custom element demo. Declare it as a private member because it will be
     * attached by the framework to a named field at `render`. Instantiate it here
     * or in the constructor or `init`.
     */
    private CustomElem customElem = new CustomElem();

    /**
     * Embedded element. Created at init there object id is available.
     */
    private CustomView embeddedView;

    public FormUser() {
        this(null, null);
    }

    public FormUser(final String id, final String initStr) {
        super(null, User.class, id, initStr, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CANCEL);
        // customElem = new CustomElem();
    }

    @Override
    public Form init() {
        super.init();
        embeddedView = new CustomView();
        return this;
    }

    /**
     * This form has attached views and needs to create the object at form creation.
     * `cancel` cascade deletes the object.
     */
    @Override
    protected boolean isCreateObjectAtInit() {
        return true;
    }

    @Override
    protected DbObject createObject() {
        final User o = (User) super.createObject();
        o.setName(initString());
        // initiate custom element after object has been created at init.
        customElem.pos_x.set(o.id());
        return o;
    }

    /** Attached views of aggregated objects. */
    @Override
    protected List<View> viewsList() {
        final ArrayList<View> ls = new ArrayList<View>();
        ls.add(new TableUserFiles(extendIdPath(objectId())));
        ls.add(new TableUserGames(extendIdPath(objectId())));
        return ls;
    }

    public String title() {
        return Util.toStr(str(User.name), "New user");
    }

    /** Custom number formatter for `float` and `double`. */
    @Override
    protected NumberFormat createNumberFormatFlt() {
        return new DecimalFormat("0.00");
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final User o = (User) object();
        beginForm(x);
        inputText(x, "Name", o, User.name, initString(), "medium");
        focus(x, User.name); // note: not `xfocus` at `render`
        inputTextArea(x, "Description", o, User.description, "b", "medium");
        inputText(x, "Password hash", o, User.passhash, "c", "medium");
        inputInt(x, "Integer", o, User.nlogins, 1, "nbr");
        inputLng(x, "Long", o, User.lng, 2, "nbr");
        inputFlt(x, "Float", o, User.flt, 3.3f, "nbr");
        inputDbl(x, "Double", o, User.dbl, 4.4, "nbr");
        inputBool(x, "Boolean", o, User.bool, true);
        inputTimestamp(x, "Timestamp", o, User.birthTime, Timestamp.valueOf("2023-01-23 20:36:00"));
        inputDateTime(x, "Date time", o, User.dateTime, Timestamp.valueOf("2023-01-24 09:00:00"));
        inputDate(x, "Date", o, User.date, Timestamp.valueOf("2023-01-25 00:00:00"));
        inputAgg(x, "Profile picture", o, User.profilePic, FormUserProfilePic.class, true);
        inputAggN(x, "Files", o, User.files, FormUserFile.class, true);
        inputRef(x, "Author", o, User.author, 0, TableAuthors.class, FormAuthor.class);
        inputRefN(x, "Books", o, User.books, null, TableBooks.class, FormBook2.class);
        inputElem(x, "customElem", customElem);
        inputElem(x, "embeddedView", embeddedView);
        endForm(x);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        // `FormDbo` writes the input fields to the `DbObject`

        final User o = (User) obj;
        if (Util.isEmpty(o.getName())) {
            xfocus(x, User.name); // note: not `focus` in handling of ui event
            throw new Exception("User name may not be empty.");
        }

        // retrieve custom element and do something with it
        CustomElem ce = (CustomElem) getElem("customElem");
        x.xalert("Custom Element: x = " + ce.pos_x.str() + ", y = " + ce.pos_y);
    }

    /** Example of custom input element. */
    public final static class CustomElem extends a {

        private final static long serialVersionUID = 1;

        public a pos_x;
        public a pos_y;

        @Override
        public void to(xwriter x) throws Throwable {
            x.p("Custom Element: x = ").inpint(pos_x).p(" y = ").inpint(pos_y).spc().ax(this, "", "update page").nl();
        }

        public void x_(final xwriter x, final String param) {
            // empty default event
        }
    }

    public final static class CustomView extends ViewTable {

        private final static long serialVersionUID = 1;

        public CustomView() {
            super(null, null, 0, 0);
        }

        @Override
        public String title() {
            return "Games";
        }

        @Override
        protected List<?> objectsList() {
            return Db.currentTransaction().get(Game.class, null, null, null);
        }

        @Override
        protected String idFrom(final Object obj) {
            return Integer.toString(((DbObject) obj).id());
        }

        @Override
        protected void renderRowCells(xwriter x, Object obj) {
            x.td().p(((Game) obj).getName());
        }
    }

}
