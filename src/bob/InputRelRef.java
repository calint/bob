package bob;

import b.a;
import b.xwriter;
import bob.View.SelectReceiverSingle;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.RelRef;

public final class InputRelRef extends a {
	private static final long serialVersionUID = 1L;
	final RelRef rel;
	final private Class<? extends View> selectViewClass; // the view to use when selecting
	final private Class<? extends Form> createFormCls;
	private int selectedId;

	public InputRelRef(final RelRef rel, final int defaultValue, final Class<? extends View> selectViewClass,
			final Class<? extends Form> createFormCls) {
		this.rel = rel;
		selectedId = defaultValue;
		this.selectViewClass = selectViewClass;
		this.createFormCls = createFormCls;
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		if (selectedId != 0) {
			final DbObject ro = tn.get(rel.getToClass(), selectedId);
			if (ro != null) {
				if (ro instanceof Titled) {
					final Titled t = (Titled) ro;
					x.p(t.getTitle());
				} else {
					x.p(ro.id());
				}
				x.spc().ax(this, "r", "✖");
			}
		}
		if (selectViewClass != null) {
			x.spc();
			x.ax(this, "s", "select");
			if (createFormCls != null) {
				x.p(" • ");
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
		final View v = selectViewClass.getConstructor().newInstance();
		v.setSelectMode(Integer.toString(selectedId), new SelectReceiverSingle() {
			private static final long serialVersionUID = 1L;

			public void onSelect(final String selected) {
				selectedId = Integer.parseInt(selected);
			}
		});
		super.bubble_event(x, this, v); // display view
	}

	/** Callback "create". */
	public void x_c(final xwriter x, final String param) throws Throwable {
		final Form f = createFormCls.getConstructor().newInstance();
		f.setSelectMode(new SelectReceiverSingle() {
			private static final long serialVersionUID = 1L;

			public void onSelect(final String selected) {
				selectedId = Integer.parseInt(selected);
			}
		});
		super.bubble_event(x, this, f); // display the form
	}

	/** Callback "remove". */
	public void x_r(final xwriter x, final String param) throws Throwable {
		selectedId = 0;
		x.xu(this);
	}

	public void save(final DbObject o) {
		rel.set(o, selectedId);
	}

	public int getSelectedId() {
		return selectedId;
	}

}
