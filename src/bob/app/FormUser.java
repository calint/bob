package bob.app;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.Db;
import db.DbObject;
import db.test.User;

public final class FormUser extends FormDbo {
	private static final long serialVersionUID = 1L;
//	private final View userFiles;

	public FormUser() {
		this(null, null);
	}

	public FormUser(final String id, final String initStr) {
		super(id);
		if (id == null) {
			// create at init pattern
			final User o = (User) Db.currentTransaction().create(User.class);
			o.setName(initStr);
			// set FormDbo objectId. this will omit createObject() call by FormDbo
			objectId = Integer.toString(o.id());
//			userFiles = new TableUserFiles(o.id());
//		} else {
//			userFiles = new TableUserFiles(Integer.parseInt(objectId));
		}
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
		// create at init pattern
		return null;
	}

//	private View authors = new TableAuthors();
	@Override
	protected void render(final xwriter x) throws Throwable {
		final User o = (User) getObject();
		beginForm(x);
		inputText(x, "Name", o, User.name, "", "medium");
		focus(x, User.name);
		inputTextArea(x, "Description", o, User.description, "", "medium");
		inputText(x, "Password hash", o, User.passhash, "", "medium");
		inputInt(x, "Integer", o, User.nlogins, 0, "nbr");
		inputLng(x, "Long", o, User.lng, 0, "nbr");
		inputFlt(x, "Float", o, User.flt, 0, "nbr");
		inputDbl(x, "Double", o, User.dbl, 0, "nbr");
		inputBool(x, "Boolean", o, User.bool, false);
		inputTimestamp(x, "Timestamp", o, User.birthTime, null);
		inputDateTime(x, "Date time", o, User.dateTime, null);
		inputDate(x, "Date", o, User.date, null);
//		inputElem(x, "authors", authors);
		inputAgg(x, "Profile picture", o, User.profilePic, FormFile.class);
		inputAggN(x, "Files", o, User.files, FormFile.class);
//		inputElem(x, "Files in an included view", "userFiles", userFiles);
		endForm(x);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
		// fields written to object by FormDbo
		final User o = (User) obj;
		if (Util.isEmpty(o.getName())) {
			xfocus(x, User.name);
			throw new Exception("User name may not be empty.");
		}
	}
}