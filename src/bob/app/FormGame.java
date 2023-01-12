package bob.app;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.DbObject;
import db.test.Game;

public final class FormGame extends FormDbo {
	private static final long serialVersionUID = 1L;

	public FormGame() {
		this(null, null);
	}

	public FormGame(final String objectId, final String initStr) {
		super(Game.class, objectId, initStr);
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
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
		// FormDbo writes the input fields to the DbObject
	}
}
