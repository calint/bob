package db;

import java.io.Serializable;
import java.util.List;

public final class DbObjects implements Serializable { // ? review select
	private static final long serialVersionUID = 1L;

	private final DbObjects dbobjects;
	private final Class<? extends DbObject> select;
	private final Query query;
	private final Order order;

	public DbObjects(final DbObjects dbobjects, final Class<? extends DbObject> select, final Query query,
			final Order order) {
		this.dbobjects = dbobjects;
		if (select == null && dbobjects == null)
			throw new RuntimeException("'select' must be specified if this DbObjects does not wrap another DbObjects.");
		if (select == null) {
			this.select = dbobjects.select;
		} else {
			this.select = select;
		}
		this.query = query;
		this.order = order;
	}

	public DbObjects(final Class<? extends DbObject> select) {
		this(null, select, null, null);
	}

	public List<DbObject> toList(final Limit limit) {
		final Query qry = new Query();
		final Order ord = new Order();
		buildQuery(qry, ord);
		return Db.currentTransaction().get(select, qry, ord, limit);
	}

	public List<DbObject> toList() {
		return toList((Limit) null);
	}

	public List<DbObject[]> toList(final Class<?>[] classes, final Limit limit) {
		final Query qry = new Query();
		final Order ord = new Order();
		buildQuery(qry, ord);
		return Db.currentTransaction().get(classes, qry, ord, limit);
	}

	public List<DbObject[]> toList(final Class<?>[] classes) {
		return toList(classes, null);
	}

	public DbObjects get(final Query qry, final Order ord) {
		return new DbObjects(this, select, qry, ord);
	}

	public DbObjects get(final Query qry) {
		return get(qry, null);
	}

	public DbObject get(final int id) {
		final Query q = new Query(select, id);
		final List<? extends DbObject> ls = Db.currentTransaction().get(select, q, null, null);
		if (ls.isEmpty())
			return null;
		return ls.get(0);
	}

	/**
	 * Convenience for get(int id).
	 * 
	 * @return null if id is null.
	 */
	public DbObject get(final String id) {
		if (id == null)
			return null;
		return get(Integer.parseInt(id));
	}

	public int getCount() {
		final Query qry = new Query();
		buildQuery(qry, null);
		return Db.currentTransaction().getCount(select, qry);
	}

	private void buildQuery(final Query qry, final Order ord) {
		if (dbobjects != null) {
			dbobjects.buildQuery(qry, ord);
		}
		if (query != null) {
			qry.and(query);
		}
		if (order != null && ord != null) {
			ord.append(order);
		}
	}

}
