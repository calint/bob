package bob;

import b.a;
import b.xwriter;
import db.DbObject;
import db.DbObjects;
import db.RelAggN;

public final class InputRelAggN extends a {
	private static final long serialVersionUID = 1L;
	final private Class<? extends Form> createFormCls;
	final Class<? extends DbObject> objCls;
	final int objId;
	final String relationName;

	public InputRelAggN(final DbObject obj, final RelAggN rel, final Class<? extends Form> createFormCls) {
		if (obj == null)
			throw new RuntimeException(
					"Element cannot be created with object being null. Try 'create at init' pattern to initiate the object before creating this element.");
		objCls = obj.getClass();
		objId = obj.id();
		relationName = rel.getName();
		this.createFormCls = createFormCls;
	}

	private RelAggN getRelation() {
		try {
			return (RelAggN) objCls.getField(relationName).get(null);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		x.ax(this, "c", "create").br().nl();
		final DbObjects dbos = getRelation().get(objId);
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
		final DbObject ro = getRelation().create(objId);
		final Form f = createFormCls.getConstructor(String.class, String.class)
				.newInstance(Integer.toString(ro.id()), null).init();
		super.bubble_event(x, this, f); // display the form
	}

	/** Callback "delete". */
	public void x_d(final xwriter x, final String param) throws Throwable {
		getRelation().delete(objId, Integer.parseInt(param));
		x.xu(this);
	}

	/** Callback "edit". */
	public void x_e(final xwriter x, final String param) throws Throwable {
		final DbObject ro = getRelation().get(objId).get(param);
		final Form f = createFormCls.getConstructor(String.class, String.class)
				.newInstance(Integer.toString(ro.id()), null).init();
		super.bubble_event(x, this, f); // display the form
	}
}
