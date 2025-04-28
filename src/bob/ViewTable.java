// reviewed: 2024-08-05
package bob;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import db.Limit;

public abstract class ViewTable extends View {
    private final static long serialVersionUID = 1;

    public final static int BIT_CLICK_ITEM = 1;

    public Container ac; // actions
    public a q; // query field
    public Table t;
    public Paging p;

    /** True to display more search section. */
    private boolean displayMoreSearch;

    /** The actions that are enabled in the table. */
    protected final int enabledTableBits;

    public ViewTable(final List<String> idPath, final int viewBits, final int tableBits, final TypeInfo typeInfo) {
        super(idPath, viewBits, typeInfo);

        enabledTableBits = tableBits;
        t.setViewTable(this);
        p.setViewTable(this);
        if ((enabledViewBits & BIT_CREATE) != 0) {
            ac.add(new Action("create " + typeInfo.name, "create"));
        }
        if ((enabledViewBits & BIT_DELETE) != 0) {
            ac.add(new Action("delete selected " + typeInfo.namePlural, "delete"));
        }
        final List<Action> actions = getActionsList();
        if (actions != null) {
            for (final Action a : actions) {
                ac.add(a);
            }
        }
    }

    @Override
    public final void to(final xwriter x) throws Throwable {
        if (isSelectMode()) {
            x.tago("div").attr("class", "attention").tagoe();
            if (isSelectModeMulti()) {
                x.p("select " + getTypeInfo().namePlural + " then click ");
                x.ax(this, "sm", "select");
            } else {
                x.p("select " + getTypeInfo().name + " by clicking on the link");
            }
            x.tage("div");
        }
        if (!ac.isEmpty()) {
            x.divh(ac, "ac").nl();
        }
        if ((enabledViewBits & BIT_SEARCH) != 0) {
            x.tago("input").default_attrs_for_element(q, "query", null);
            final String eid = id();
            if ((enabledViewBits & BIT_CREATE) != 0) {
                x.attr("onkeypress", "if(event.keyCode!=13)return true;clearTimeout(ui.debounceTimeoutId);$x('" + eid
                        + " new');return false;");
            }
            x.attr("oninput", "$b(this);ui.debounce(()=>$x('" + eid + " q'),500)");
            x.attr("value", q.str());
            x.tagoe();
            if (hasMoreSearchSection()) {
                x.spc();
                x.ax(this, "more", displayMoreSearch ? "less" : "more");
                if (displayMoreSearch) {
                    x.divo(this, "ms", null).tagoe();
                    renderMoreSearchSection(x);
                    x.div_();
                    x.spc();
                    x.ax(this, "q", "search");
                } else {
                    x.focus(q); // note: focus sticks to first call and cannot be overridden
                }
            } else {
                x.focus(q); // note: focus sticks to first call and cannot be overridden
            }
        }
        final boolean infiniteScroll = isInfiniteScroll();
        if (infiniteScroll) {
            p.upd();
            p.setPage(1);
        }
        x.divh(t).nl();
        if (p.isEnabled() && !infiniteScroll) {
            x.divh(p, "pgr").nl();
        }
    }

    @Override
    protected final void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (from instanceof Action) {
            final String code = ((Action) from).code();
            if ("create".equals(code) && (enabledViewBits & BIT_CREATE) != 0) {
                onActionCreate(x, q.str());
                return;
            }
            if ("delete".equals(code) && (enabledViewBits & BIT_DELETE) != 0) {
                if (getSelectedIds().isEmpty()) {
                    x.xalert("No " + getTypeInfo().namePlural + " selected.");
                    return;
                }
                onActionDelete(x);
                refresh(x);
                return;
            }
            onAction(x, (Action) from);
            refresh(x);
            return;
        }
        if (from == p) {
            // event from pager
            refresh(x);
            return;
        }
        // event unknown by this element
        super.bubble_event(x, from, o);
    }

    private void refresh(final xwriter x) throws Throwable {
        x.xu(t); // update table
        if (p.isEnabled() && !isInfiniteScroll()) {
            // update paging
            x.xu(p);
        }
        x.xfocus(q);
        x.xscroll_to_top();
    }

    /** Callback for change in query field. */
    public final void x_q(final xwriter x, final String s) throws Throwable {
        if (p.isEnabled()) {
            p.setPage(1); // reset the page
            x.xu(p); // update paging
        }
        x.xu(t); // update table
    }

    /** Callback for press enter in query field. */
    public final void x_new(final xwriter x, final String s) throws Throwable {
        if ((enabledViewBits & BIT_CREATE) != 0) {
            onActionCreate(x, q.str());
        }
    }

    /** Callback for "more" search. */
    public final void x_more(final xwriter x, final String s) throws Throwable {
        displayMoreSearch = !displayMoreSearch;
        if (!displayMoreSearch) {
            clearMoreSearchSection(x);
            p.setPage(1);
        }
        x.xu(this);
    }

    @Override
    protected final Set<String> getSelectedIds() {
        return t.getSelectedIds();
    }

    /**
     * Renders a link with object id that calls onRowClick(...) with command null.
     *
     * @param x
     * @param o
     * @param linkText
     */
    protected final void renderLink(final xwriter x, final Object o, final String linkText) {
        if (isSelectMode() && !isSelectModeMulti()) {
            final String id = getIdFrom(o);
            x.ax(this, "ss " + id, linkText);
        } else if ((enabledTableBits & ViewTable.BIT_CLICK_ITEM) != 0) {
            final String id = getIdFrom(o);
            x.ax(this, "clk " + id, linkText);
        } else {
            x.p(linkText);
        }
    }

    /**
     * Renders a link with id and command that calls onRowClick(...) with specified
     * command.
     *
     * @param x
     * @param id
     * @param cmd
     * @param linkText
     */
    protected final void renderLink(final xwriter x, final String id, final String cmd, final String linkText) {
        x.ax(this, "c " + cmd + " " + id, linkText);
    }

    /** Callback for click on row. */
    public final void x_clk(final xwriter x, final String s) throws Throwable {
        if ((enabledTableBits & ViewTable.BIT_CLICK_ITEM) != 0) {
            onRowClick(x, s, null);
        }
    }

    /** Callback for click on row. */
    public final void x_c(final xwriter x, final String s) throws Throwable {
        final int i = s.indexOf(' ');
        final String type = s.substring(0, i);
        final String id = s.substring(i + 1);
        onRowClick(x, id, type);
    }

    /** Callback for click on "select" when in select multi mode. */
    public final void x_sm(final xwriter x, final String s) throws Throwable {
        getSelectReceiverMulti().onSelect(getSelectedIds());
        super.bubble_event(x, this, "close");
    }

    /** Callback for click on row in select single mode. */
    public final void x_ss(final xwriter x, final String s) throws Throwable {
        getSelectReceiverSingle().onSelect(s);
        super.bubble_event(x, this, "close");
    }

    // -----------------------------------------------------------------------------------
    // override these to specialize table

    protected boolean hasMoreSearchSection() {
        return false;
    }

    protected void renderMoreSearchSection(final xwriter x) {
    }

    protected void clearMoreSearchSection(final xwriter x) throws Throwable {
    }

    @Override
    protected int getObjectsPerPageCount() {
        return 0;
    }

    protected boolean isInfiniteScroll() {
        return false;
    }

    @Override
    protected int getObjectsCount() {
        return 0;
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
    }

    @Override
    protected void onActionDelete(final xwriter x) throws Throwable {
    }

    @Override
    protected void onAction(final xwriter x, final Action act) throws Throwable {
    }

    protected void renderHeaders(final xwriter x) {
    }

    protected void renderRowCells(final xwriter x, final Object obj) {
    }

    /**
     * Called when a row is clicked.
     *
     * @param cmd specified at renderLinked(...), null if default.
     **/
    protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
    }

    // -----------------------------------------------------------------------------------

    public final static class Paging extends a {

        private final static long serialVersionUID = 1;

        private int currentPage; // page starting at 0
        private int objectsPerPage;
        private int objectsCount; // objects in view
        private int npages; // pages
        private ViewTable vt;
        public a pg; // current page

        public void setViewTable(final ViewTable vt) {
            this.vt = vt;
            objectsPerPage = vt.getObjectsPerPageCount();
            pg.set(currentPage + 1);
        }

        public void upd() {
            objectsCount = vt.getObjectsCount();
            npages = objectsCount / objectsPerPage;
            if (objectsCount % objectsPerPage != 0) {
                npages++;
            }
        }

        @Override
        public void to(final xwriter x) throws Throwable {
            upd();
            if (objectsCount == 0) {
                return;
            }
            x.p(objectsCount);
            x.p(' ');
            if (objectsCount == 1) {
                x.p(vt.getTypeInfo().name);
            } else {
                x.p(vt.getTypeInfo().namePlural);
            }
            x.p(". ");
            if (npages > 1) {
                x.p("Page ");
                // ! pg may be more than npages when deleting, adjust
                x.inp(pg, null, "nbr-small center", null, null, this, "p", null, null);
                x.p(" of ").p(npages).p(". ");
                if (currentPage != 0) {
                    x.ax(this, "pg prv", "Previous");
                    x.p(" ");
                }
                if (currentPage < npages - 1) {
                    x.ax(this, "pg nxt", "Next");
                }
            }
        }

        public void x_pg(final xwriter x, final String param) throws Throwable {
            if ("prv".equals(param)) {
                currentPage--;
                if (currentPage < 0) {
                    currentPage = 0;
                }
            }
            if ("nxt".equals(param)) {
                nextPage();
            }
            pg.set(currentPage + 1);
            super.bubble_event(x);
        }

        public boolean nextPage() {
            currentPage++;
            if (currentPage >= npages) {
                currentPage = npages - 1;
                return false;
            }
            pg.set(currentPage + 1);
            return true;
        }

        public void x_p(final xwriter x, final String param) throws Throwable {
            final int n;
            try {
                n = pg.toint();
            } catch (final Throwable t) {
                x.xfocus(pg);
                x.xalert("Enter a page number.");
                return;
            }
            currentPage = n - 1;
            if (currentPage < 0) {
                currentPage = 0;
                pg.set(currentPage + 1);
            } else if (currentPage >= npages) {
                currentPage = npages - 1;
                pg.set(currentPage + 1);
            }
            super.bubble_event(x);
        }

        public int getLimitStart() {
            return currentPage * objectsPerPage;
        }

        public int getLimitCount() {
            return objectsPerPage;
        }

        public boolean isEnabled() {
            return objectsPerPage != 0;
        }

        /** Sets current page starting at 1. */
        public void setPage(final int page) {
            currentPage = page - 1;
            pg.set(page);
        }

        public Limit getLimit() {
            return new Limit(getLimitStart(), getLimitCount());
        }

    }

    public final static class Table extends a {

        final static long serialVersionUID = 1;
        public Container cbs; // checkboxes
        public a is; // infinite scroll marker

        private ViewTable vt; // the parent
        private final LinkedHashSet<String> selectedIds = new LinkedHashSet<String>();

        public void setViewTable(final ViewTable vt) {
            this.vt = vt;
        }

        @Override
        public void to(final xwriter x) throws Throwable {
            final boolean inifiniteScroll = vt.isInfiniteScroll();
            if (inifiniteScroll) {
                vt.p.setPage(1);
            }
            final List<?> ls = vt.getObjectsList();
            if (!ls.isEmpty()) {
                x.table("t").nl();
                x.tr();
                if ((vt.enabledViewBits & View.BIT_SELECT) != 0 || vt.isSelectModeMulti()) {
                    // header for the checkbox
                    x.th();
                }
                vt.renderHeaders(x);
                x.nl();
                cbs.clear();
                renderRows(x, ls);
                x.table_();
                x.script();
                if (inifiniteScroll) {
                    x.p("window.onscroll=(e)=>{if((window.innerHeight+window.scrollY)>=document.body.offsetHeight){$x('"
                            + id() + " is');}}");
                } else {
                    // disable infinite scroll event
                    x.p("window.onscroll=null;");
                }
                x.script_();
            }
        }

        private void renderRows(final xwriter x, final List<?> ls) throws Throwable {
            for (final Object o : ls) {
                x.tr();
                if ((vt.enabledViewBits & View.BIT_SELECT) != 0 || vt.isSelectModeMulti()) {
                    // render checkbox
                    final String id = vt.getIdFrom(o);
                    x.td();
                    final Checkbox cb = new Checkbox(id, selectedIds.contains(id));
                    // add to container where the element will get a unique name in the context.
                    // the parent of the checkbox will be the container.
                    cbs.add(cb);
                    // checkbox now has parent and name. render it.
                    cb.to(x);
                }
                vt.renderRowCells(x, o);
                x.nl();
            }
            if (vt.isInfiniteScroll()) {
                // insertion point for more rows
                x.p("<tr id=" + is.id() + ">");
            }
        }

        @Override
        protected void bubble_event(final xwriter js, final a from, final Object o) throws Throwable {
            // event bubbled from child
            if (from instanceof Checkbox && ((vt.enabledViewBits & View.BIT_SELECT) != 0 || vt.isSelectModeMulti())) {
                final String id = ((Checkbox) from).getId();
                if ("checked".equals(o)) {
                    selectedIds.add(id);
                    return;
                }
                if ("unchecked".equals(o)) {
                    selectedIds.remove(id);
                    return;
                }
            }
            // event unknown by this element, bubble to parent
            super.bubble_event(js, from, o);
        }

        /** Callback for infinite scroll. */
        public void x_is(final xwriter y, final String s) throws Throwable {
            if (!vt.p.nextPage()) {
                // if there are no more pages
                return;
            }

            final xwriter x = y.xub(is, false, false); // render more rows at the insertion tag
            renderRows(x, vt.getObjectsList());
            y.xube();
        }

        protected Set<String> getSelectedIds() {
            return selectedIds;
        }

    }

}
