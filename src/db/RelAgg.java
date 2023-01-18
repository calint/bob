package db;

/** Aggregation One-to-One. */
public final class RelAgg extends DbRelation {
	public RelAgg(final Class<? extends DbObject> toCls) {
		super(toCls);
	}

	@Override
	void init(final DbClass dbcls) {
		relFld = new FldRel();
		relFld.cls = cls;
		final DbClass dbc = Db.getDbClassForJavaClass(cls);
		relFld.tableName = dbc.tableName;
		relFld.name = name;
		dbcls.allFields.add(relFld);
	}

	/** @returns 0 if id is null */
	public int getId(final DbObject ths) {
		final Object objId = ths.fieldValues.get(relFld);
		if (objId == null)
			return 0;
		return (Integer) objId;
	}

	public DbObject get(final DbObject ths, final boolean createIfNone) {
		final int id = getId(ths);
		if (id == 0) {
			if (createIfNone) {
				final DbObject o = Db.currentTransaction().create(toCls);
				ths.set(relFld, o.id());
				return o;
			}
			return null;
		}
		return Db.currentTransaction().get(toCls, new Query(toCls, id), null, null).get(0);
	}

	public void delete(final DbObject ths) {
		final DbObject o = get(ths, false);
		if (o == null)
			return;
		Db.currentTransaction().delete(o);
		ths.set(relFld, 0);
	}

	@Override
	protected void cascadeDelete(final DbObject ths) {
		final int toId = getId(ths);
		if (toId == 0)
			return;
		final DbObject o = get(ths, false);
		Db.currentTransaction().delete(o);
	}
}
