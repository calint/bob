//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import b.a;
import b.xwriter;
import bob.View.SelectReceiverMulti;
import bob.View.SelectReceiverSingle;
import db.Db;
import db.DbObject;
import db.DbObjects;
import db.DbTransaction;
import db.RelRefN;

public final class InputRelRefN extends a {
    private final static long serialVersionUID = 1;

    private final Class<? extends DbObject> objCls;
    private final String relationName;
    private final Class<? extends View> selectViewClass; // the view to use when selecting
    private final Class<? extends Form> createFormCls; // the form used to create object
    private final LinkedHashSet<String> initialSelectedIds; // the initial ids from object
    private final LinkedHashSet<String> selectedIds; // current selected ids
    private final String itemSeparator;

    public InputRelRefN(final DbObject obj, final RelRefN rel, final Set<String> defaultValues,
            final Class<? extends View> selectViewClass, final Class<? extends Form> createFormCls,
            final String itemSeparator) {
        objCls = rel.getFromClass();
        relationName = rel.getName();
        this.selectViewClass = selectViewClass;
        this.createFormCls = createFormCls;
        initialSelectedIds = new LinkedHashSet<String>();
        selectedIds = new LinkedHashSet<String>();
        this.itemSeparator = itemSeparator;
        if (obj != null) {
            final DbObjects dbos = rel.get(obj);
            for (final DbObject o : dbos.toList()) {
                final String idstr = Integer.toString(o.id());
                selectedIds.add(idstr);
                initialSelectedIds.add(idstr);
            }
            return;
        }
        if (defaultValues == null) {
            return;
        }
        selectedIds.addAll(defaultValues);
    }

    public final RelRefN relation() {
        try {
            return (RelRefN) objCls.getField(relationName).get(null);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Set<String> selectedIds() {
        return selectedIds;
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        if (selectViewClass != null) {
            x.ax(this, "s", "select", "act");
            if (createFormCls != null) {
                x.p(" • ");
            }
        }
        if (createFormCls != null) {
            x.ax(this, "c", "create", "act");
        }
        if (selectViewClass != null || createFormCls != null) {
            x.br();
        }
        final Class<? extends DbObject> toCls = relation().getToClass();
        for (final String s : selectedIds) {
            final DbObject o = tn.get(toCls, s);
            if (o == null) {
                continue;
            }
            if (o instanceof Titled) {
                final Titled t = (Titled) o;
                x.p(t.title());
            } else {
                x.p(o.id());
            }
            x.spc().ax(this, "r " + o.id(), "✖", "act").p(itemSeparator);
        }
    }

    public void save(final DbObject o) {
        for (final String s : selectedIds) {
            if (initialSelectedIds.contains(s)) {
                continue;
            }
            final int id = Integer.parseInt(s);
            relation().add(o, id);
            initialSelectedIds.add(s);
        }
        final ArrayList<String> removedIds = new ArrayList<String>();
        for (final String s : initialSelectedIds) {
            if (selectedIds.contains(s)) {
                continue;
            }
            final int id = Integer.parseInt(s);
            relation().remove(o, id);
            removedIds.add(s);
        }
        for (final String id : removedIds) {
            initialSelectedIds.remove(id);
        }
    }

    /** Callback "select". */
    public void x_s(final xwriter x, final String param) throws Throwable {
        final View v = selectViewClass.getConstructor().newInstance().init();
        v.setSelectMode(selectedIds, new SelectReceiverMulti() {
            private final static long serialVersionUID = 1;

            public void onSelect(final Set<String> selected) {
                selectedIds.clear();
                selectedIds.addAll(selected);
            }
        });
        super.bubble_event(x, this, v); // display the table
    }

    /** Callback "create". */
    public void x_c(final xwriter x, final String param) throws Throwable {
        final Form f = createFormCls.getConstructor().newInstance().init();
        f.setSelectMode(new SelectReceiverSingle() {
            private final static long serialVersionUID = 1;

            public void onSelect(final String selected) {
                selectedIds.add(selected);
            }
        });
        super.bubble_event(x, this, f); // display the form
    }

    /** Callback "remove". */
    public void x_r(final xwriter x, final String param) throws Throwable {
        selectedIds.remove(param);
        x.xu(this);
    }

}
