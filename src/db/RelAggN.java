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
		final DbClass dbClsTo = Db.dbClassForJavaClass(toCls);
		final DbTransaction tn = Db.currentTransaction();
		if (dbClsTo.cascadeDelete) {
			final DbObject o = tn.get(toCls, toId);
			delete(thsId, o);
			return;
		}

		// no cascade delete
		tn.flush();
		tn.deleteReferencesToObject(dbClsTo, toId);

		final StringBuilder sb = new StringBuilder(128);
		sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(DbObject.id.name).append("=")
				.append(toId).append(" and ").append(relFld.name).append("=").append(thsId);

		if (!Db.cluster_on) {
			tn.execSql(sb);
		} else {
			Db.execClusterSql(sb.toString());
		}
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
		final DbClass dbClsTo = Db.dbClassForJavaClass(toCls);
		final DbTransaction tn = Db.currentTransaction();
		if (dbClsTo.cascadeDelete || !dbClsTo.referingRefN.isEmpty()
				|| (Db.enable_update_referring_tables && !dbClsTo.referingRef.isEmpty())) {
			final List<DbObject> ls = get(thsId).toList();
			for (final DbObject o : ls) {
				tn.delete(o);
			}
			return;
		}

		// cascade not needed, nor referring classes
		tn.flush(); // flush dirty objects

		final StringBuilder sb = new StringBuilder(128);
		sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(relFld.name).append("=")
				.append(thsId);

		if (!Db.cluster_on) {
			tn.execSql(sb);
		} else {
			Db.execClusterSql(sb.toString());
		}

//		final DbTransaction tn = Db.currentTransaction();
//		final List<DbObject> ls = get(thsId).toList();
//		for (final DbObject o : ls) {
//			tn.delete(o);
//		}
	}
}
