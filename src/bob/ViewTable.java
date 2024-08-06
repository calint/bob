// reviewed: 2024-08-05
package bob;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import db.Limit;

public abstract class ViewTable extends View {
    private static final long serialVersionUID = 1;

    public final static int BIT_CLICK_ITEM = 1;

    public Container ac; // actions
    public a q; // query field
    public Table t;
    public Paging p;
    public a ms; // more search section

    /** True to display more search section. */
    private boolean ms_display;

    /** The actions that are enabled in the table. */
    final protected int enabledTableBits;

    public ViewTable(final List<String> idPath, final int viewBits, final int tableBits, final TypeInfo typeInfo) {
        super(idPath, viewBits, typeInfo);
        enabledTableBits = tableBits;
        t.setTableView(this);
        p.setTableView(this);
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
            x.script().xfocus(q).script_();
            if (hasMoreSearchSection()) {
                x.spc();
                x.ax(this, "more", "more");
                x.spc();
                x.tago("div").default_attrs_for_element(ms, "more-search", null);
                if (!ms_display) {
                    x.attr("hidden");
                }
                x.tagoe();
                renderMoreSearchSection(x);
                x.spc();
                x.ax(this, "q", "search");
                x.tage("div").nl();
            }
        }
        final boolean inifiniteScroll = isInifiniteScroll();
        if (inifiniteScroll) {
            p.upd();
            p.setPage(1);
        }
        x.divh(t).nl();
        if (p.isEnabled() && !inifiniteScroll) {
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
        if (p.isEnabled() && !isInifiniteScroll()) {
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
        ms_display = !ms_display;
        x.p("$('").p(ms.id()).p("').hidden=").p(!ms_display).pl(";");
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

    @Override
    protected int getObjectsPerPageCount() {
        return 0;
    }

    protected boolean isInifiniteScroll() {
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
        private static final long serialVersionUID = 1;

        private int currentPage; // page starting at 0
        private int objectsPerPage;
        private int objectsCount; // objects in view
        private int npages; // pages
        private ViewTable tv;
        public a pg; // current page

        public void setTableView(final ViewTable tv) {
            this.tv = tv;
            objectsPerPage = tv.getObjectsPerPageCount();
            pg.set(currentPage + 1);
        }

        public void upd() {
            objectsCount = tv.getObjectsCount();
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
                x.p(tv.getTypeInfo().name);
            } else {
                x.p(tv.getTypeInfo().namePlural);
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

        public void setPage(final int page) {
            currentPage = page - 1;
            pg.set(page);
        }

        public Limit getLimit() {
            return new Limit(getLimitStart(), getLimitCount());
        }
    }

    public final static class Table extends a {
        static final long serialVersionUID = 1;
        public Container cbs; // checkboxes
        public a is; // infinite scroll marker

        private ViewTable tv; // the parent
        private final LinkedHashSet<String> selectedIds = new LinkedHashSet<String>();

        public void setTableView(final ViewTable tv) {
            this.tv = tv;
        }

        @Override
        public void to(final xwriter x) throws Throwable {
            final boolean inifiniteScroll = tv.isInifiniteScroll();
            if (inifiniteScroll) {
                tv.p.setPage(1);
            }
            final List<?> ls = tv.getObjectsList();
            if (!ls.isEmpty()) {
                x.table("t").nl();
                x.tr();
                if ((tv.enabledViewBits & View.BIT_SELECT) != 0 || tv.isSelectModeMulti()) {
                    // header for the checkbox
                    x.th();
                }
                tv.renderHeaders(x);
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
                if ((tv.enabledViewBits & View.BIT_SELECT) != 0 || tv.isSelectModeMulti()) {
                    // render checkbox
                    final String id = tv.getIdFrom(o);
                    x.td();
                    final Checkbox cb = new Checkbox(id, selectedIds.contains(id));
                    // add to container where the element will get a unique name in the context.
                    // the parent of the checkbox will be the container.
                    cbs.add(cb);
                    // checkbox now has parent and name. render it.
                    cb.to(x);
                }
                tv.renderRowCells(x, o);
                x.nl();
            }
            if (tv.isInifiniteScroll()) {
                // insertion point for more rows
                x.p("<tr id=" + is.id() + ">");
            }
        }

        @Override
        protected void bubble_event(final xwriter js, final a from, final Object o) throws Throwable {
            // event bubbled from child
            if (from instanceof Checkbox && ((tv.enabledViewBits & View.BIT_SELECT) != 0 || tv.isSelectModeMulti())) {
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
            if (!tv.p.nextPage()) {
                // if there are no more pages
                return;
            }

            final xwriter x = y.xub(is, false, false); // render more rows at the insertion tag
            renderRows(x, tv.getObjectsList());
            y.xube();
        }

        protected Set<String> getSelectedIds() {
            return selectedIds;
        }
    }

}
