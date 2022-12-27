package bob.app;

import b.osltgt;
import b.xwriter;
import bob.Form;
import db.DbClass;

public class FormDbClass extends Form {
	private static final long serialVersionUID = 1L;

	private final String javaClassName;

	public FormDbClass(final DbClass dbclass) {
		super(null, dbclass.getJavaClass().getName(), BIT_CLOSE);
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
