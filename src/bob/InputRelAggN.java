package bob;

import b.a;
import b.xwriter;
import db.Db;
import db.DbObject;
import db.DbObjects;
import db.DbTransaction;
import db.RelAggN;

public final class InputRelAggN extends a {
	private static final long serialVersionUID = 1L;
	final RelAggN rel;
	final private Class<? extends Form> createFormCls;
	final int objId;

	public InputRelAggN(final DbObject obj, final RelAggN rel, final Class<? extends Form> createFormCls) {
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
		final DbObjects dbos = rel.get(o);
		x.ax(this, "c", "create").br().nl();
		for (final DbObject ro : dbos.toList()) {
			final String txt;
			if (ro instanceof Titled) {
				final Titled t = (Titled) ro;
				txt = t.getTitle();
			} else {
				txt = Integer.toString(ro.id());
			}
			x.ax(this, "e " + ro.id(), txt);
			x.spc().ax(this, "d " + ro.id(), "✖").br().nl();
		}
	}

	/** Callback "create". */
	public void x_c(final xwriter x, final String param) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		final DbObject ro = rel.create(o);
		final Form f = createFormCls.getConstructor(String.class, String.class).newInstance(Integer.toString(ro.id()),
				null);
		super.bubble_event(x, this, f); // display the form
	}

	/** Callback "delete". */
	public void x_d(final xwriter x, final String param) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		rel.delete(o, Integer.parseInt(param));
		x.xu(this);
	}

	/** Callback "edit". */
	public void x_e(final xwriter x, final String param) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		final DbObject ro = rel.get(o).get(param);
		final Form f = createFormCls.getConstructor(String.class, String.class).newInstance(Integer.toString(ro.id()),
				null);
		super.bubble_event(x, this, f); // display the form
	}
}
