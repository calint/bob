package bob.app;

import java.util.List;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.Game;
import db.test.User;

public final class FormUserGame extends FormDbo {
	private static final long serialVersionUID = 1L;

	public FormUserGame() {
		this(null, null, null);
	}

	public FormUserGame(final List<String> idPath, final String objectId, final String initStr) {
		super(idPath, Game.class, objectId, initStr);
	}

	public String getTitle() {
		return Util.toStr(getStr(Game.name), "New game");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Game o = (Game) getObject();
		beginForm(x);
		inputText(x, "Name", o, Game.name, getInitStr(), "medium");
		focus(x, Game.name);
		endForm(x);
	}

	@Override
	protected DbObject createObject() {
		final List<String> idPath = getIdPath();
		final DbTransaction tn = Db.currentTransaction();
		final User u = (User) tn.get(User.class, idPath.get(0));
		return u.createGame();
	}

	@Override
	protected DbObject getObject() {
		final String oid = getObjectId();
		if (oid == null)
			return null;
		final List<String> idPath = getIdPath();
		final DbTransaction tn = Db.currentTransaction();
		final User u = (User) tn.get(User.class, idPath.get(0));
		return (Game) u.getGames().get(oid);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
		// FormDbo writes the input fields to the DbObject
	}
}
