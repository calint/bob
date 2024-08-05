// reviewed: 2024-08-05
package bob.app;

import b.osltgt;
import b.xwriter;
import bob.Form;
import db.DbClass;

public final class FormDbClass extends Form {
	private static final long serialVersionUID = 1;

	private final String javaClassName;

	public FormDbClass(final DbClass dbclass) {
		super(null, dbclass.getJavaClass().getName(), null, BIT_CLOSE);
		javaClassName = dbclass.getJavaClass().getName();
	}

	public String getTitle() {
		return javaClassName;
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.tago("div").attr("class", "output").tagoe();
		jem.Main.main(javaClassName, new osltgt(x.outputstream()));
		x.div_();
	}
}
