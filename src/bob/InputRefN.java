package bob;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import b.a;
import b.xwriter;
import bob.ViewTable.SelectReceiverMulti;
import db.Db;
import db.DbObject;
import db.DbObjects;
import db.DbTransaction;
import db.RelRefN;

public final class InputRefN extends a {
	private static final long serialVersionUID = 1L;
	final private RelRefN rel;
	final private Class<? extends ViewTable> viewTableSelectClass;
	final private Class<? extends Form> createFormCls;
	final private LinkedHashSet<String> initialSelectedIds;
	final private LinkedHashSet<String> selectedIds;
	final private String itemSeparator;
	private int objId;
	private boolean selectedIdsInitiated;

	public InputRefN(final RelRefN rel, final Class<? extends ViewTable> viewTableSelectClass,
			final Class<? extends Form> createFormCls, final String itemSeparator) {
		this.rel = rel;
		this.viewTableSelectClass = viewTableSelectClass;
		this.createFormCls = createFormCls;
		initialSelectedIds = new LinkedHashSet<String>();
		selectedIds = new LinkedHashSet<String>();
		this.itemSeparator = itemSeparator;
	}

	public void refreshCurrentIds(final DbObject obj) {
		objId = obj.id();
		final DbObjects dbos = rel.get(obj);
		for (final DbObject o : dbos.toList()) {
			final String idstr = Integer.toString(o.id());
			initialSelectedIds.add(idstr);
		}
		if (!selectedIdsInitiated) { // if first call
			selectedIds.addAll(initialSelectedIds);
			selectedIdsInitiated = true;
		}
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		if (viewTableSelectClass != null) {
			x.ax(this, "s", "select");
			if (createFormCls != null) {
				x.spc();
			}
		}
		if (createFormCls != null) {
			x.ax(this, "c", "create");
		}
		if (viewTableSelectClass != null || createFormCls != null) {
			x.br();
		}
		for (final String s : selectedIds) {
			final int id = Integer.parseInt(s);
			final DbObject o = tn.get(rel.getToClass(), id);
			if (o == null) {
				continue;
			}
			if (o instanceof Titled) {
				final Titled t = (Titled) o;
				x.p(t.getTitle());
			} else {
				x.p(o.id());
			}
			x.spc().ax(this, "r " + o.id(), "[x]").p(itemSeparator);
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
		t.setSelectMode(selectedIds, new SelectReceiverMulti() {
			private static final long serialVersionUID = 1L;

			public void onSelect(final Set<String> selected) {
				selectedIds.clear();
				selectedIds.addAll(selected);
			}
		});
		super.bubble_event(x, this, t); // display t
	}

	/** Callback "create". */
	public void x_c(final xwriter x, final String param) {

	}

	/** Callback "remove". */
	public void x_r(final xwriter x, final String param) throws Throwable {
		selectedIds.remove(param);
		x.xu(this);
	}

	public void save() {
		final DbTransaction tn = Db.currentTransaction();
		final DbObject o = tn.get(rel.getFromClass(), objId);
		for (final String s : selectedIds) {
			if (initialSelectedIds.contains(s)) {
				continue;
			}
			final int id = Integer.parseInt(s);
			rel.add(o, id);
			initialSelectedIds.add(s);
		}
		final ArrayList<String> removedIds = new ArrayList<String>();
		for (final String s : initialSelectedIds) {
			if (selectedIds.contains(s)) {
				continue;
			}
			final int id = Integer.parseInt(s);
			rel.remove(o, id);
			removedIds.add(s);
		}
		for (final String s : removedIds) {
			initialSelectedIds.remove(s);
		}
	}

	public Set<String> getSelectedIds() {
		return selectedIds;
	}
}
