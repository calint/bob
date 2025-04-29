//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.util.List;

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
    private final List<String> idPath;

    public InputRelAgg(final List<String> idPath, final DbObject obj, final RelAgg rel,
            final Class<? extends Form> createFormCls) {
        if (obj == null) {
            throw new RuntimeException(
                    "Element cannot be created with object being null. Try 'create at init' pattern to initiate the object before creating this element.");
        }
        this.idPath = idPath;
        objCls = obj.getClass();
        objId = obj.id();
        relationName = rel.getName();
        this.createFormCls = createFormCls;
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
            x.spc().ax(this, "d", "✖");
        } else {
            x.ax(this, "c", "create");
        }
    }

    /** Callback "create". */
    public void x_c(final xwriter x, final String param) throws Throwable {
        final Form f = createFormCls.getConstructor(List.class, String.class, String.class)
                .newInstance(idPath, null, null).init();
        super.bubble_event(x, this, f); // display the form
    }

    /** Callback "remove". */
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
        final Form f = createFormCls.getConstructor(List.class, String.class, String.class)
                .newInstance(idPath, Integer.toString(ro.id()), null).init();
        super.bubble_event(x, this, f); // display the form
    }

}
