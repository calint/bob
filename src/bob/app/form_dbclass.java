package bob.app;

import b.osltgt;
import b.xwriter;
import bob.form;
import db.DbClass;

public class form_dbclass extends form {
	private static final long serialVersionUID = 1L;

	private final String javaClassName;

	public form_dbclass(final DbClass dbclass) {
		super(null, dbclass.getJavaClass().getName(), BIT_CLOSE);
		javaClassName = dbclass.getJavaClass().getName();
	}

	public String getTitle() {
		return javaClassName;
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.divo("output");
		jem.Main.main(javaClassName, new osltgt(x.outputstream()));
		x.div_();
	}
}
