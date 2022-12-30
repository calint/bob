package bob;

import b.a;
import b.xwriter;
import bob.ViewTable.SelectReceiverSingle;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.RelRef;

public final class InputRef extends a {
	private static final long serialVersionUID = 1L;
	final private RelRef rel;
	final private Class<? extends ViewTable> viewTableSelectClass;
	final private Class<? extends Form> createFormCls;
	private int selectedId;
	private int objId;
	private boolean selectedIdInitiated;

	public InputRef(final RelRef rel, final Class<? extends ViewTable> viewTableSelectClass,
			final Class<? extends Form> createFormCls) {
		this.rel = rel;
		this.viewTableSelectClass = viewTableSelectClass;
		this.createFormCls = createFormCls;
	}

	public void refreshCurrentId(final DbObject obj) {
		if (selectedIdInitiated) // do this only once
			return;
		objId = obj.id();
		selectedId = rel.getId(obj);
		selectedIdInitiated = true;
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		if (objId != 0) {
			final DbObject o = tn.get(rel.getFromClass(), objId);
			if (selectedId != 0) {
				final DbObject ro = tn.get(rel.getToClass(), selectedId);
				if (ro != null) {
					if (ro instanceof Titled) {
						final Titled t = (Titled) ro;
						x.p(t.getTitle());
					} else {
						x.p(ro.id());
					}
					x.spc().ax(this, "r " + o.id(), "[x]");
				}
			}
		}
		if (viewTableSelectClass != null) {
			x.spc();
			x.ax(this, "s", "find");
			if (createFormCls != null) {
				x.spc();
			}
		}
		if (createFormCls != null) {
			x.ax(this, "c", "create");
		}
		x.nl();
	}

	/**
	 * Callback "select".
	 *
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void x_s(final xwriter x, final String param) throws Throwable {
		final ViewTable t = viewTableSelectClass.newInstance();
		t.setSelectMode(Integer.toString(selectedId), new SelectReceiverSingle() {
			private static final long serialVersionUID = 1L;

			public void onSelect(final String selected) {
				selectedId = Integer.parseInt(selected);
			}
		});
		super.bubble_event(x, this, t); // display t
	}

	/** Callback "create". */
	public void x_c(final xwriter x, final String param) {

	}

	/** Callback "remove". */
	public void x_r(final xwriter x, final String param) throws Throwable {
		selectedId = 0;
		x.xu(this);
	}

	public void save() {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		rel.set(o, selectedId);
	}

	public int getSelectedId() {
		return selectedId;
	}
}
