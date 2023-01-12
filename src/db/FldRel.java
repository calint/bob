package db;

/** Field/column that refers to an id. It may be null or 0. */
final class FldRel extends DbField {
	public FldRel() {
		super("int", 0, null, true, false);
	}

	@Override
	protected void sql_updateValue(final StringBuilder sb, final DbObject o) {
		final int id = o.getInt(this);
		if (id == 0) {
			sb.append("null");
			return;
		}
		sb.append(id);
	}
}
