//
// reviewed: 2024-08-05
//           2025-04-28
//           2025-05-02
//
package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

public final class Tabs extends a {

    private final static long serialVersionUID = 1;

    public final static class Tab extends a {

        private final static long serialVersionUID = 1;

        private final a elem;

        public Tab(final String title, final a elem) {
            this.elem = elem;
            elem.parent(this);
            elem.name("e");
            set(title);
        }

        @Override
        public a child(final String id) {
            final a e = super.child(id);
            if (e != null) {
                return e;
            }
            if ("e".equals(id)) {
                return elem;
            }
            return null;
        }

        public void to(final xwriter x, final boolean isActive) throws Throwable {
            if (isActive) {
                x.divo(this, "at", null).tagoe();
                x.p(str());
                x.div_();
                return;
            }
            x.divo(this, "iat", null).tagoe();
            // click callback
            x.ax(this, "c", str());
            x.div_().nl();
        }

        /** Callback for click. */
        public void x_c(final xwriter x, final String param) throws Throwable {
            super.bubble_event(x);
        }

    }

    final private ArrayList<Tab> tabs = new ArrayList<Tab>();
    public a ae; // active element

    public void add(final Tab t) {
        t.parent(this);
        t.name(Integer.toString(tabs.size()));
        if (tabs.isEmpty()) {
            // if first element added replace active element with the element in the tab
            t.elem.replace(ae);
        }
        tabs.add(t);
    }

    @Override
    public a child(final String id) {
        final a e = super.child(id);
        if (e != null) {
            return e;
        }
        return tabs.get(Integer.parseInt(id));
    }

    public boolean isEmpty() {
        return tabs.isEmpty();
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        for (final Tab t : tabs) {
            t.to(x, t.elem == ae);
        }
        x.divh(ae, "ae").nl();
    }

    @Override
    protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (from instanceof Tab) {
            // activated tab
            final Tab t = (Tab) from;
            t.elem.replace(ae);
            x.xu(this);
            return;
        }
        // unknown event
        super.bubble_event(x, from, o);
    }

}
