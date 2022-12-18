package bob.app;

import b.xwriter;
import bob.form;
import db.DbClass;

public class form_dbclass extends form {
	private static final long serialVersionUID = 1L;

	private final DbClass dbclass;

	public form_dbclass(final DbClass dbclass) {
		super(null, dbclass.getJavaClass().getName(), BIT_CLOSE);
		this.dbclass = dbclass;
	}

	public String getTitle() {
		return dbclass.getJavaClass().getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.divo("output");
		jem.Main.main(dbclass.getJavaClass().getName(), x.outputstream());
		x.div_();
	}
}
