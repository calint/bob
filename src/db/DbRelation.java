package db;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

/** Abstract relation. */
public abstract class DbRelation implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * the class where the relation was declared. initiated by db after all classes
	 * have been loaded
	 */
	protected Class<? extends DbObject> cls;

	/** the table name of cls. initiated by db after all classes have been loaded */
	protected String tableName;

	/**
	 * the name of the field that declared it. initiated by db after all classes
	 * have been loaded
	 */
	protected String name;

	/** the class that the relations refers to */
	final protected Class<? extends DbObject> toCls;

	/** the table name of toCls */
	final protected String toTableName;

	/**
	 * Field used by relations {@link RelAgg} and {@link RelRef}. May be null in
	 * case relation does not use column. Example {@link RelRefN}. The field may
	 * return null or 0.
	 */
	FldRel relFld;

	public DbRelation(final Class<? extends DbObject> toCls) {
		this.toCls = toCls;
		toTableName = Db.tableNameForJavaClass(toCls);
	}

	/**
	 * Called after all DbClasses have been created. Relation can here add fields
	 * and indexes.
	 */
	void init(final DbClass c) {
	}

	/**
	 * Called after all tables have been created. Relation ensures necessary indexes
	 * exist and match the expected specification.
	 */
	void ensureIndexes(final Statement stmt, final DatabaseMetaData dbm) throws Throwable {
	}

	public final String getName() {
		return name;
	}

	public final Class<? extends DbObject> getFromClass() {
		return cls;
	}

	public final Class<? extends DbObject> getToClass() {
		return toCls;
	}

	/** @return true if cascadeDelete is to be called when an object is deleted. */
	boolean cascadeDeleteNeeded() {
		return true;
	}

	/** Called when source object is deleted and cascade delete needed. */
	void cascadeDelete(final DbObject ths) {
	}

	@Override
	public String toString() {
		return name;
	}
}
