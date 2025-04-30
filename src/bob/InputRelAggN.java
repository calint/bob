//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import b.a;
import b.xwriter;
import db.DbObject;
import db.DbObjects;
import db.RelAggN;

public final class InputRelAggN extends a {

    private final static long serialVersionUID = 1;

    private final Class<? extends Form> createFormCls;
    private final Class<? extends DbObject> objCls;
    private final int objId;
    private final String relationName;
    private final Elem.IdPath idPath;

    /**
     * @param idPath        Id path to parent of `obj`.
     * @param obj           The object.
     * @param rel           The relation from object.
     * @param createFormCls The form used to create the relation target object.
     */
    public InputRelAggN(final Elem.IdPath idPath, final DbObject obj, final RelAggN rel,
            final Class<? extends Form> createFormCls) {
        if (obj == null) {
            throw new RuntimeException(
                    "Element cannot be created with object being null. Try 'create at init' pattern to initiate the object before creating this element.");
        }
        objCls = obj.getClass();
        objId = obj.id();
        this.idPath = idPath;
        relationName = rel.getName();
        this.createFormCls = createFormCls;
    }

    public RelAggN relation() {
        try {
            return (RelAggN) objCls.getField(relationName).get(null);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        x.ax(this, "c", "create").br();
        final DbObjects dbos = relation().get(objId);
        for (final DbObject ro : dbos.toList()) {
            final String txt;
            if (ro instanceof Titled) {
                final Titled t = (Titled) ro;
                txt = t.title();
            } else {
                txt = Integer.toString(ro.id());
            }
            final int oid = ro.id();
            x.ax(this, "e " + oid, txt);
            x.spc().ax(this, "d " + oid, "✖").br().nl();
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
        relation().delete(objId, Integer.parseInt(param)); // ? idPath
        x.xu(this);
    }

    /** Callback "edit". */
    public void x_e(final xwriter x, final String param) throws Throwable {
        final Form f = createFormCls.getConstructor(Elem.IdPath.class, String.class, String.class)
                .newInstance(Elem.IdPath.extend(idPath, objId), param, null).init();
        super.bubble_event(x, this, f); // display the form
    }

}
