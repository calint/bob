package db;

import java.util.List;

/** Association One-to-One. */
public final class RelRef extends DbRelation {
	private static final long serialVersionUID = 1L;

	public RelRef(final Class<? extends DbObject> toCls) {
		super(toCls);
	}

	@Override
	void init(final DbClass c) {
		relFld = new FldRel();
		relFld.cls = cls;
		final DbClass dbc = Db.getDbClassForJavaClass(cls);
		relFld.tableName = dbc.tableName;
		relFld.name = name;
		c.allFields.add(relFld);

		if (Db.enable_update_referring_tables) {
			// add an index on the referring field
			final Index ix = new Index(relFld);
			ix.cls = cls;
			ix.name = relFld.name;
			ix.tableName = relFld.tableName;
			dbc.allIndexes.add(ix);
		}

		final DbClass todbcls = Db.dbClassForJavaClass(toCls);
		todbcls.referingRef.add(this);
	}

	/** Set 0 to remove reference. */
	public void set(final DbObject ths, final int trgId) {
		ths.set(relFld, trgId);
	}

//	public void remove(final DbObject ths) {
//		set(ths, 0);
//	}

	/** @returns 0 if id is null. */
	public int getId(final DbObject ths) {
		final Object objId = ths.fieldValues.get(relFld);
		if (objId == null)
			return 0;
		return (Integer) objId;
	}

	public DbObject get(final DbObject ths) {
		final int id = getId(ths);
		if (id == 0)
			return null;
		final List<? extends DbObject> ls = Db.currentTransaction().get(toCls, new Query(toCls, id), null, null);
		if (ls.isEmpty())
			// null
			return null;
		return ls.get(0);
	}

	@Override
	boolean cascadeDeleteNeeded() {
		return false;
	}

}
