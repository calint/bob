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
		return Db.currentTransaction().get(toCls, id);
	}

	public void delete(final DbObject ths) {
		cascadeDelete(ths);
		ths.set(relFld, 0);
	}

	@Override
	protected void cascadeDelete(final DbObject ths) {
		final int toId = getId(ths);
		if (toId == 0)
			return;

		// need to delete
		final DbClass dbClsTo = Db.dbClassForJavaClass(toCls);
		final DbTransaction tn = Db.currentTransaction();
		if (dbClsTo.cascadeDelete || !dbClsTo.referingRefN.isEmpty()
				|| Db.enable_update_referring_tables && !dbClsTo.referingRef.isEmpty()) {
			final DbObject o = get(ths, false);
			tn.delete(o);
			return;
		}

		// cascade not necessary, no referring classes
		tn.flush(); // flush dirty objects

		final StringBuilder sb = new StringBuilder(128);
		sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(DbObject.id.name).append("=")
				.append(toId);
		if (!Db.cluster_on) {
			tn.execSql(sb);
		} else {
			Db.execClusterSql(sb.toString());
		}
	}
}
