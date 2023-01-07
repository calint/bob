package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbObject;
import db.test.User;

public final class FormUser extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormUser() {
		this(null, null);
	}

	public FormUser(final String objectId, final String initStr) {
		super(objectId);
		this.initStr = initStr;
	}

	public String getTitle() {
		final User o = (User) getObject();
		return o == null ? "New user" : o.getName();
	}

	@Override
	protected DbObject getObject() {
		return Db.currentTransaction().get(User.class, getObjectId());
	}

	@Override
	protected DbObject createObject() {
		return Db.currentTransaction().create(User.class);
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final User o = (User) getObject();
		beginForm(x);
		inputText(x, "Name", o, User.name, "medium", initStr);
		focus(x, User.name);
		inputTextArea(x, "Description", o, User.description, "medium", "");
		inputText(x, "Password hash", o, User.passhash, "medium", "");
		inputInt(x, "Integer", o, User.nlogins, "nbr", 0);
		inputLng(x, "Long", o, User.lng, "nbr", 0);
		inputFlt(x, "Float", o, User.flt, "nbr", 0);
		inputDbl(x, "Double", o, User.dbl, "nbr", 0);
		inputBool(x, "Boolean", o, User.bool, false);
		inputTimestamp(x, "Timestamp", o, User.birthTime, null);
		inputDate(x, "Date", o, User.date, null);
		inputDateTime(x, "Date time", o, User.dateTime, null);
		endForm(x);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
//		final Category o = (Category) obj;
//		o.setName(getStr(Category.name));
	}
}