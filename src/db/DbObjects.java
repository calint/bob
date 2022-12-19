package db;

import java.io.Serializable;
import java.util.List;

public final class DbObjects implements Serializable {
	private static final long serialVersionUID = 1L;

	private final DbObjects dbobjects;
	private final Class<? extends DbObject> select;
	private final Query query;
	private final Order order;

	public DbObjects(final DbObjects dbobjects, final Class<? extends DbObject> select, final Query query,
			final Order order) {
		this.dbobjects = dbobjects;
		this.select = select;
		this.query = query;
		this.order = order;
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
