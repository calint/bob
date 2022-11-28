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
		final DbClass toDbCls = Db.instance().dbClassForJavaClass(toCls);
		relFld.tableName = toDbCls.tableName;
		toDbCls.allFields.add(relFld);

		// add an index to target class
		final Index ix = new Index(relFld);
		ix.cls = toCls;
		ix.name = relFld.name;
		ix.tableName = relFld.tableName;

		final DbClass dbc = Db.instance().getDbClassForJavaClass(toCls);
		dbc.allIndexes.add(ix);
	}

	public DbObject create(final DbObject ths) {
		final DbObject o = Db.currentTransaction().create(toCls);
		o.set(relFld, ths.id());
		return o;
	}

	public List<DbObject> get(final DbObject ths, final Query qry, final Order ord, final Limit lmt) {
		final Query q = new Query(ths.getClass(), ths.id()).and(this);
		if (qry != null)
			q.and(qry);
		return Db.currentTransaction().get(toCls, q, ord, lmt);
	}

	public int getCount(final DbObject ths, final Query qry) {
		final Query q = new Query(ths.getClass(), ths.id()).and(this);
		if (qry != null)
			q.and(qry);
		return Db.currentTransaction().getCount(toCls, q);
	}

	public void delete(final DbObject ths, final int toId) {
		final DbObject o = Db.currentTransaction().get(toCls, new Query(toCls, toId), null, null).get(0);
		delete(ths, o);
	}

	public void delete(final DbObject ths, final DbObject o) {
		if (!o.fieldValues.containsKey(relFld) || o.getInt(relFld) != ths.id())
			throw new RuntimeException(ths.getClass().getName() + "[" + ths.id() + "] does not contain "
					+ toCls.getName() + "[" + o.id() + "] in relation '" + this.name + "'");

		Db.currentTransaction().delete(o);
	}

	@Override
	void cascadeDelete(final DbObject ths) {
		final DbClass dbClsTo = Db.instance().dbClassForJavaClass(toCls);
		if (dbClsTo.cascadeDelete) {
			final List<DbObject> ls = get(ths, null, null, null);
			for (final DbObject o : ls) {
				Db.currentTransaction().delete(o);
			}
			return;
		}

		final StringBuilder sb = new StringBuilder(128);
		sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(relFld.name).append("=")
				.append(ths.id());

		Db.currentTransaction().execSql(sb);
	}
}
