//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import b.a;
import b.xwriter;
import bob.View.SelectReceiverSingle;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.RelRef;

public final class InputRelRef extends a {

    private final static long serialVersionUID = 1;

    private final Class<? extends DbObject> objCls;
    private final String relationName;
    private final Class<? extends View> selectViewClass; // the view to use when selecting
    private final Class<? extends Form> createFormCls;
    private int selectedId;

    public InputRelRef(final DbObject obj, final RelRef rel, final int defaultValue,
            final Class<? extends View> selectViewClass, final Class<? extends Form> createFormCls) {
        objCls = rel.getFromClass();
        relationName = rel.getName();
        selectedId = obj == null ? defaultValue : rel.getId(obj);
        this.selectViewClass = selectViewClass;
        this.createFormCls = createFormCls;
    }

    private RelRef getRelation() {
        try {
            return (RelRef) objCls.getField(relationName).get(null);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        if (selectedId != 0) {
            final DbObject ro = tn.get(getRelation().getToClass(), selectedId);
            if (ro != null) { // ? dangling reference
                if (ro instanceof Titled) {
                    final Titled t = (Titled) ro;
                    x.p(t.getTitle());
                } else {
                    x.p(ro.id());
                }
                x.spc().ax(this, "r", "✖");
            }
        }
        if (selectViewClass != null) {
            x.spc();
            x.ax(this, "s", "select");
            if (createFormCls != null) {
                x.p(" • ");
            }
        }
        if (createFormCls != null) {
            x.ax(this, "c", "create");
        }
    }

    public void save(final DbObject o) {
        getRelation().set(o, selectedId);
    }

    public int getSelectedId() {
        return selectedId;
    }

    /** Callback "select". */
    public void x_s(final xwriter x, final String param) throws Throwable {
        final View v = selectViewClass.getConstructor().newInstance();
        v.setSelectMode(Integer.toString(selectedId), new SelectReceiverSingle() {
            private final static long serialVersionUID = 1;

            public void onSelect(final String selected) {
                selectedId = Integer.parseInt(selected);
            }
        });
        super.bubble_event(x, this, v); // display view
    }

    /** Callback "create". */
    public void x_c(final xwriter x, final String param) throws Throwable {
        final Form f = createFormCls.getConstructor().newInstance().init();
        f.setSelectMode(new SelectReceiverSingle() {
            private final static long serialVersionUID = 1;

            public void onSelect(final String selected) {
                selectedId = Integer.parseInt(selected);
            }
        });
        super.bubble_event(x, this, f); // display the form
    }

    /** Callback "remove". */
    public void x_r(final xwriter x, final String param) throws Throwable {
        selectedId = 0;
        x.xu(this);
    }

}
