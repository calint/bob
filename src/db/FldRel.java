// reviewed: 2024-08-05
package db;

/** Field/column that refers to an id. It may be null or 0. */
final class FldRel extends DbField {
	public FldRel() {
		super("int", 0, null, null, true, false);
	}

	@Override
	protected void sql_updateValue(final StringBuilder sb, final DbObject o) {
		final int id = getId(o);
		if (id == 0) {
			sb.append("null");
			return;
		}
		sb.append(id);
	}

	public void setId(final DbObject ths, final int v) {
		setObj(ths, v);
	}

	public int getId(final DbObject ths) {
		return (Integer) getObj(ths);
	}
}
