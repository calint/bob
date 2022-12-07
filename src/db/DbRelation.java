package db;

import java.sql.DatabaseMetaData;
import java.sql.Statement;

/** Abstract relation. */
public abstract class DbRelation {
	/**
	 * the class where the relation was declared. initiated by db after all classes
	 * have been loaded
	 */
	Class<? extends DbObject> cls;

	/** the table name of cls. initiated by db after all classes have been loaded */
	String tableName;

	/**
	 * the name of the field that declared it. initiated by db after all classes
	 * have been loaded
	 */
	String name;

	/** the class that the relations refers to */
	final Class<? extends DbObject> toCls;

	/** the table name of toCls */
	final String toTableName;

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

	public final Class<? extends DbObject> getToClass() {
		return toCls;
	}

	/** @return true if cascadeDelete is to be called when an object is deleted. */
	boolean cascadeDeleteNeeded() {
		return true;
	}

	void cascadeDelete(final DbObject ths) {
	}

	@Override
	public String toString() {
		return name;
	}
}
