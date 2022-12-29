package bob;

import java.util.HashSet;

import b.a;
import b.xwriter;
import db.Db;
import db.DbObject;
import db.DbObjects;
import db.DbTransaction;
import db.RelRefN;

public final class InputRefN extends a {
	private static final long serialVersionUID = 1L;
	final private RelRefN rel;
	final private Class<? extends View> selectViewCls;
	final private Class<? extends Form> createFormCls;
	final private HashSet<String> currentIds;
	final private HashSet<String> selectedIds;
	final private String itemSeparator;
	private int objId;
	private boolean selectedIdsInitiated;

	public InputRefN(final RelRefN rel, final Class<? extends View> selectViewCls,
			final Class<? extends Form> createFormCls, final String itemSeparator) {
		this.rel = rel;
		this.selectViewCls = selectViewCls;
		this.createFormCls = createFormCls;
		this.currentIds = new HashSet<String>();
		this.selectedIds = new HashSet<String>();
		this.itemSeparator = itemSeparator;
	}

	public void refreshCurrentIds(final DbObject obj) {
		objId = obj.id();
		final DbObjects dbos = rel.get(obj);
		for (final DbObject o : dbos.toList()) {
			final String idstr = Integer.toString(o.id());
			currentIds.add(idstr);
		}
		if (!selectedIdsInitiated) { // if first call 
			selectedIds.addAll(currentIds);
			selectedIdsInitiated = true;
		}
	}

	@Override
	public void to(xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		if (selectViewCls != null) {
			x.ax(this, "s", "select");
			if (createFormCls != null) {
				x.spc();
			}
		}
		if (createFormCls != null) {
			x.ax(this, "c", "create");
		}
		if (selectViewCls != null || createFormCls != null) {
			x.br();
		}
		for (final String s : selectedIds) {
			final int id = Integer.parseInt(s);
			final DbObject o = tn.get(rel.getToClass(), id);
			if (o == null)
				continue;
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

	/** Callback "select". */
	public void x_s(final xwriter x, final String param) {

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
			if (currentIds.contains(s))
				continue;
			final int id = Integer.parseInt(s);
			rel.add(o, id);
		}
		for (final String s : currentIds) {
			if (selectedIds.contains(s))
				continue;
			final int id = Integer.parseInt(s);
			rel.remove(o, id);
		}
	}
}
