//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.util.List;

/** Lazy collection of DbObjects. */
public final class DbObjects {

    private final DbObjects dbObjects;
    private final Class<? extends DbObject> select; // ? review select. generalize with Class[]
    private final Query query;
    private final Order order;

    public DbObjects(final DbObjects dbobjects, final Class<? extends DbObject> select, final Query query,
            final Order order) {
        this.dbObjects = dbobjects;
        if (dbobjects == null && select == null) {
            throw new RuntimeException("'select' must be specified if this DbObjects does not wrap a DbObjects.");
        }
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

    public DbObject first() {
        final List<DbObject> ls = toList(select, new Limit(0, 1));
        if (ls.isEmpty()) {
            return null;
        }
        return ls.get(0);
    }

    public List<DbObject> toList(final Class<?> cls, final Limit limit) {
        final Query qry = new Query();
        final Order ord = new Order();
        buildQuery(qry, ord);
        return Db.currentTransaction().get(cls, qry, ord, limit);
    }

    public List<DbObject> toList(final Limit limit) {
        return toList(select, limit);
    }

    public List<DbObject> toList() {
        return toList(select, null);
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

    /** @return A new lazy collection with additional query and order. */
    public DbObjects get(final Query qry, final Order ord) {
        return new DbObjects(this, select, qry, ord);
    }

    /** @return A new lazy collection with additional query. */
    public DbObjects get(final Query qry) {
        return get(qry, null);
    }

    public DbObject get(final int id) {
        final Query qry = new Query();
        buildQuery(qry, null);
        qry.and(new Query(select, id));
        final List<? extends DbObject> ls = Db.currentTransaction().get(select, qry, null, null);
        if (ls.isEmpty()) {
            return null;
        }
        return ls.get(0);
    }

    /**
     * Convenience for get(int id).
     *
     * @return null if id is null otherwise get(id).
     */
    public DbObject get(final String id) {
        if (id == null) {
            return null;
        }
        return get(Integer.parseInt(id));
    }

    public int getCount() {
        final Query qry = new Query();
        buildQuery(qry, null);
        return Db.currentTransaction().getCount(select, qry);
    }

    private void buildQuery(final Query qry, final Order ord) {
        if (dbObjects != null) {
            dbObjects.buildQuery(qry, ord);
        }
        if (query != null) {
            qry.and(query);
        }
        if (ord == null) {
            // ignore order if irrelevant
            return;
        }
        if (order != null) {
            ord.append(order);
        }
    }

}
