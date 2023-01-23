package bob.app;

import java.util.ArrayList;
import java.util.List;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import bob.View;
import db.Db;
import db.DbObject;
import db.test.User;

public final class FormUser extends FormDbo implements FormDbo.CreateObjectAtInit {
	private static final long serialVersionUID = 3L;

	public FormUser() {
		this(null, null);
	}

	public FormUser(final String id, final String initStr) {
		super(null, User.class, id, initStr, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CANCEL);
	}

	@Override
	protected DbObject createObject() {
		final User o = (User) super.createObject();
		o.setName(getInitStr());
		return o;
	}

	@Override
	protected void cancel(final xwriter x) throws Throwable {
		if (isNewObject() && !hasBeenSaved()) {
			Db.currentTransaction().delete(getObject());
		}
	}

	@Override
	protected List<View> getViewsList() {
		final int oid = Integer.parseInt(getObjectId());
		final ArrayList<View> ls = new ArrayList<View>();
		ls.add(new TableUserFiles(oid));
		ls.add(new TableUserGames(oid));
		return ls;
	}

	public String getTitle() {
		return Util.toStr(getStr(User.name), "New user");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final User o = (User) getObject();
		beginForm(x);
		inputText(x, "Name", o, User.name, "", "medium");
		focus(x, User.name);
		inputTextArea(x, "Description", o, User.description, "", "medium");
		inputText(x, "Password hash", o, User.passhash, "", "medium");
		inputInt(x, "Integer", o, User.nlogins, 1, "nbr");
		inputLng(x, "Long", o, User.lng, 2, "nbr");
		inputFlt(x, "Float", o, User.flt, 3.3f, "nbr");
		inputDbl(x, "Double", o, User.dbl, 4.4, "nbr");
		inputBool(x, "Boolean", o, User.bool, true);
		inputTimestamp(x, "Timestamp", o, User.birthTime, null);
		inputDateTime(x, "Date time", o, User.dateTime, null);
		inputDate(x, "Date", o, User.date, null);
		inputAgg(x, "Profile picture", makeExtendedIdPath(o.id()), o, User.profilePic, FormUserProfilePic.class);
		inputAggN(x, "Files", makeExtendedIdPath(o.id()), o, User.files, FormUserFile.class);
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