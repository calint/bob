package bob;

import b.a;
import b.xwriter;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.RelAgg;

public final class InputRelAgg extends a {
	private static final long serialVersionUID = 1L;
	final RelAgg rel;
	final private Class<? extends Form> createFormCls;
	final int objId;

	public InputRelAgg(final DbObject obj, final RelAgg rel, final Class<? extends Form> createFormCls) {
		if (obj == null)
			throw new RuntimeException(
					"Element cannot be created with object being null. Try 'create at init' pattern to initiate the object before creating this element.");
		objId = obj.id();
		this.rel = rel;
		this.createFormCls = createFormCls;
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		final DbObject ro = rel.get(o, false);
		if (ro != null) {
			if (ro instanceof Titled) {
				final Titled t = (Titled) ro;
				x.p(t.getTitle());
			} else {
				x.p(ro.id());
			}
			x.spc().ax(this, "r", "✖");
			if (createFormCls != null) {
				x.spc();
			}
		}
		if (createFormCls != null && ro == null) {
			x.ax(this, "c", "create");
		}
		x.nl();
	}

	/** Callback "create". */
	public void x_c(final xwriter x, final String param) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		final DbObject ro = rel.get(o, true);
		final Form f = createFormCls.getConstructor(String.class, String.class).newInstance(Integer.toString(ro.id()),
				null);
		super.bubble_event(x, this, f); // display the form
	}

	/** Callback "remove". */
	public void x_r(final xwriter x, final String param) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		rel.delete(o);
		x.xu(this);
	}
}
