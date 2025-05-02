//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import b.a;
import b.xwriter;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.RelAgg;

public final class InputRelAgg extends a {

    private final static long serialVersionUID = 1;

    private final Class<? extends Form> createFormCls;
    private final Class<? extends DbObject> objCls;
    private final int objId;
    private final String relationName;
    private final Elem.IdPath idPath;
    private final boolean allowDelete;

    /**
     * @param idPath        Id path to parent of `obj`.
     * @param obj           The object.
     * @param rel           The relation from object.
     * @param createFormCls The form used to create the relation target object.
     */
    public InputRelAgg(final Elem.IdPath idPath, final DbObject obj, final RelAgg rel,
            final Class<? extends Form> createFormCls, final boolean allowDelete) {
        if (obj == null) {
            throw new RuntimeException(
                    "Element cannot be created with object being null. Try 'create at init' pattern to initiate the object before creating this element.");
        }
        this.idPath = idPath;
        objCls = obj.getClass();
        objId = obj.id();
        relationName = rel.getName();
        this.createFormCls = createFormCls;
        this.allowDelete = allowDelete;
    }

    public RelAgg relation() {
        try {
            return (RelAgg) objCls.getField(relationName).get(null);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        final DbObject o = tn.get(objCls, objId);
        final DbObject ro = relation().get(o, false);
        if (ro != null) {
            final String txt;
            if (ro instanceof Titled) {
                final Titled t = (Titled) ro;
                txt = t.title();
            } else {
                txt = Integer.toString(ro.id());
            }
            x.ax(this, "e", txt);
            if (allowDelete) {
                x.spc().ax(this, "d", "✖", "act");
            }
        } else if (createFormCls != null) {
            x.ax(this, "c", "create", "act");
        }
    }

    /** Callback "create". */
    public void x_c(final xwriter x, final String param) throws Throwable {
        final Form f = createFormCls.getConstructor(Elem.IdPath.class, String.class, String.class)
                .newInstance(Elem.IdPath.extend(idPath, objId), null, null).init();
        super.bubble_event(x, this, f); // display the form
    }

    /** Callback "delete". */
    public void x_d(final xwriter x, final String param) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        final DbObject o = tn.get(objCls, objId); // ? idPath
        relation().delete(o);
        x.xu(this);
    }

    /** Callback "edit". */
    public void x_e(final xwriter x, final String param) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        final DbObject o = tn.get(objCls, objId); // ? idPath
        final DbObject ro = relation().get(o, false);
        final Form f = createFormCls.getConstructor(Elem.IdPath.class, String.class, String.class)
                .newInstance(Elem.IdPath.extend(idPath, objId), Integer.toString(ro.id()), null).init();
        super.bubble_event(x, this, f); // display the form
    }

}
