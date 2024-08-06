// reviewed: 2024-08-05
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
    private static final long serialVersionUID = 1;

    final Class<? extends DbObject> objCls;
    final String relationName;
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

    private RelRefN getRelation() {
        try {
            return (RelRefN) objCls.getField(relationName).get(null);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        final DbTransaction tn = Db.currentTransaction();
        if (selectViewClass != null) {
            x.ax(this, "s", "select");
            if (createFormCls != null) {
                x.p(" • ");
            }
        }
        if (createFormCls != null) {
            x.ax(this, "c", "create");
        }
        if (selectViewClass != null || createFormCls != null) {
            x.br();
        }
        final Class<? extends DbObject> toCls = getRelation().getToClass();
        for (final String s : selectedIds) {
            final int id = Integer.parseInt(s);
            final DbObject o = tn.get(toCls, id);
            if (o == null) {
                continue;
            }
            if (o instanceof Titled) {
                final Titled t = (Titled) o;
                x.p(t.getTitle());
            } else {
                x.p(o.id());
            }
            x.spc().ax(this, "r " + o.id(), "✖").p(itemSeparator);
        }
        x.nl();
    }

    /** Callback "select". */
    public void x_s(final xwriter x, final String param) throws Throwable {
        final View t = selectViewClass.getConstructor().newInstance();
        t.setSelectMode(selectedIds, new SelectReceiverMulti() {
            private static final long serialVersionUID = 1;

            public void onSelect(final Set<String> selected) {
                selectedIds.clear();
                selectedIds.addAll(selected);
            }
        });
        super.bubble_event(x, this, t); // display the table
    }

    /** Callback "create". */
    public void x_c(final xwriter x, final String param) throws Throwable {
        final Form f = createFormCls.getConstructor().newInstance().init();
        f.setSelectMode(new SelectReceiverSingle() {
            private static final long serialVersionUID = 1;

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

    public void save(final DbObject o) {
        for (final String s : selectedIds) {
            if (initialSelectedIds.contains(s)) {
                continue;
            }
            final int id = Integer.parseInt(s);
            getRelation().add(o, id);
            initialSelectedIds.add(s);
        }
        final ArrayList<String> removedIds = new ArrayList<String>();
        for (final String s : initialSelectedIds) {
            if (selectedIds.contains(s)) {
                continue;
            }
            final int id = Integer.parseInt(s);
            getRelation().remove(o, id);
            removedIds.add(s);
        }
        for (final String id : removedIds) {
            initialSelectedIds.remove(id);
        }
    }

    public Set<String> getSelectedIds() {
        return selectedIds;
    }

}
