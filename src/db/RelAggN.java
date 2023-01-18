package db;

import java.util.List;

/** Aggregation One-to-Many. */
public final class RelAggN extends DbRelation {
	public RelAggN(final Class<? extends DbObject> toCls) {
		super(toCls);
	}

	@Override
	void init(final DbClass dbcls) {
		relFld = new FldRel();
		relFld.cls = toCls;
		relFld.name = dbcls.tableName + "_" + name;
		final DbClass toDbCls = Db.dbClassForJavaClass(toCls);
		relFld.tableName = toDbCls.tableName;
		toDbCls.allFields.add(relFld);

		// add an index to target class
		final Index ix = new Index(relFld);
		ix.cls = toCls;
		ix.name = relFld.name;
		ix.tableName = relFld.tableName;

		final DbClass dbc = Db.getDbClassForJavaClass(toCls);
		dbc.allIndexes.add(ix);
	}

	/** @param thsId source object id. */
	public DbObject create(final int thsId) {
		final DbObject o = Db.currentTransaction().create(toCls);
		o.set(relFld, thsId);
		return o;
	}

	public DbObject create(final DbObject ths) {
		return create(ths.id());
	}

	/** @param thsId source object id. */
	public DbObjects get(final int thsId) {
		final Query q = new Query(cls, thsId).and(this);
		return new DbObjects(null, toCls, q, null);
	}

	public DbObjects get(final DbObject ths) {
		return get(ths.id());
	}

	public void delete(final DbObject ths, final int toId) {
		delete(ths.id(), toId);
	}

	/** @param thsId source object id. */
	public void delete(final int thsId, final int toId) {
		final DbObject o = Db.currentTransaction().get(toCls, new Query(toCls, toId), null, null).get(0);
		delete(thsId, o);
	}

	public void delete(final DbObject ths, final DbObject o) {
		delete(ths.id(), o);
	}

	/** @param thsId source object id. */
	public void delete(final int thsId, final DbObject o) {
		if (!o.fieldValues.containsKey(relFld) || o.getInt(relFld) != thsId)
			throw new RuntimeException(cls.getName() + "[" + thsId + "] does not contain " + toCls.getName() + "["
					+ o.id() + "] in relation '" + name + "'");

		Db.currentTransaction().delete(o);
	}

	public void deleteAll(final DbObject ths) {
		deleteAll(ths.id());
	}

	/** @param thsId source object id. */
	public void deleteAll(final int thsId) {
		cascadeDelete(thsId);
	}

	@Override
	protected void cascadeDelete(final DbObject ths) {
		cascadeDelete(ths.id());
	}

	private void cascadeDelete(final int thsId) {
		final DbTransaction tn = Db.currentTransaction();
		final List<DbObject> ls = get(thsId).toList();
		for (final DbObject o : ls) {
			tn.delete(o);
		}
	}
}
