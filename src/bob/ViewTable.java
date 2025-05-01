//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import db.Limit;

public abstract class ViewTable extends View {

    private final static long serialVersionUID = 1;

    public final static int BIT_RENDER_LINKED_ITEM = 1;
    public final static int BIT_HAS_MORE_SEARCH_SECTION = 2;

    public Container ac; // actions
    public Table t;
    public Paging p;

    /** True to display "more search" section. */
    private boolean displayMoreSearch;

    /** The actions that are enabled in the table. */
    private final int tableBits;

    public ViewTable() {
        this(null, null, 0, 0);
    }

    public ViewTable(final TypeInfo typeInfo, final IdPath idPath, final int viewBits, final int tableBits) {
        super(typeInfo, idPath, viewBits);

        this.tableBits = tableBits;
    }

    @Override
    public View init() throws Throwable {
        super.init();

        t.init(this);
        p.init(this);

        final List<Action> actions = actionsList();
        for (final Action a : actions) {
            ac.add(a);
        }
        return this;
    }

    @Override
    public final void to(final xwriter x) throws Throwable {
        if (isSelectMode()) {
            x.tago("div").attr("class", "atn").tagoe();
            if (isSelectModeMulti()) {
                x.p("select " + typeInfo().namePlural() + " then click: ");
                x.ax(this, "sm", "select", "act");
            } else {
                x.p("select " + typeInfo().name() + " by clicking on the link");
            }
            x.div_().nl();
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
            if ((tableBits & BIT_HAS_MORE_SEARCH_SECTION) != 0) {
                x.spc();
                x.ax(this, "more", displayMoreSearch ? "less" : "more");
                if (displayMoreSearch) {
                    x.tago("div").attr("class", "ms").tagoe();
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
        x.nl();
        x.divh(t).nl();
        if (p.isEnabled() && !p.isSinglePage() && !infiniteScroll) {
            x.divh(p, "pgr").nl();
        }
    }

    @Override
    protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (from == p) {
            // event from pager
            refresh(x);
            return;
        }
        // event unknown by this element
        super.bubble_event(x, from, o);
    }

    @Override
    protected void refresh(final xwriter x) throws Throwable {
        x.xu(t); // update table
        if (p.isEnabled() && !isInfiniteScroll()) {
            // update paging
            x.xu(p);
        }
        x.xfocus(q);
        x.xscroll_to_top();
    }

    @Override
    protected final Set<String> selectedIds() {
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
            final String id = idFrom(o);
            x.ax(this, "ss " + id, linkText);
        } else if ((tableBits & ViewTable.BIT_RENDER_LINKED_ITEM) != 0) {
            final String id = idFrom(o);
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
        if ((tableBits & ViewTable.BIT_RENDER_LINKED_ITEM) != 0) {
            onRowClick(x, s, null);
        }
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

    /** Callback for click on row. */
    public final void x_c(final xwriter x, final String s) throws Throwable {
        final int i = s.indexOf(' ');
        final String type = s.substring(0, i);
        final String id = s.substring(i + 1);
        onRowClick(x, id, type);
    }

    /** Callback for click on "select" when in select multi mode. */
    public final void x_sm(final xwriter x, final String s) throws Throwable {
        getSelectReceiverMulti().onSelect(selectedIds());
        super.bubble_event(x, this, "close");
    }

    /** Callback for click on row in select single mode. */
    public final void x_ss(final xwriter x, final String s) throws Throwable {
        getSelectReceiverSingle().onSelect(s);
        super.bubble_event(x, this, "close");
    }

    //
    // override methods below to customize
    //

    protected abstract void renderRowCells(final xwriter x, final Object obj);

    @Override
    public String title() {
        return getClass().getName();
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

    @Override
    protected String idFrom(Object obj) {
        return null;
    }

    /**
     * Called when a link in a row is clicked.
     *
     * @param cmd Specified at renderLinked(...), null if default.
     **/
    protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
    }

    protected void renderMoreSearchSection(final xwriter x) {
    }

    protected void clearMoreSearchSection(final xwriter x) throws Throwable {
    }

    protected boolean isInfiniteScroll() {
        return false;
    }

    //
    // Inner classes
    //

    public final static class Paging extends a {

        private final static long serialVersionUID = 1;

        public a pg; // current page

        private int currentPage; // page starting at 0
        private int objectsPerPage;
        private int objectsCount; // objects in view
        private int npages; // pages
        private ViewTable vt; // reference to parent

        public void init(final ViewTable vt) {
            this.vt = vt;
            objectsPerPage = vt.objectsPerPageCount();
            pg.set(currentPage + 1);
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
                x.p(vt.typeInfo().name());
            } else {
                x.p(vt.typeInfo().namePlural());
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

        public void upd() {
            objectsCount = vt.objectsCount();
            npages = objectsCount / objectsPerPage;
            if (objectsCount % objectsPerPage != 0) {
                npages++;
            }
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

        public boolean isSinglePage() {
            return npages == 0;
        }

        /** Sets current page starting at 1. */
        public void setPage(final int page) {
            currentPage = page - 1;
            pg.set(page);
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

        public Limit getLimit() {
            return new Limit(getLimitStart(), getLimitCount());
        }

        /** Callback when clicking "previous" and "next" in pager. */
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

        /** Callback on pressing "enter" in page field. */
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

    }

    public final static class Table extends a {

        final static long serialVersionUID = 1;

        public Container cbs; // checkboxes
        public a is; // infinite scroll marker

        private ViewTable vt; // the parent
        private final LinkedHashSet<String> selectedIds = new LinkedHashSet<String>();

        public void init(final ViewTable vt) {
            this.vt = vt;
        }

        @Override
        public void to(final xwriter x) throws Throwable {
            final boolean infiniteScroll = vt.isInfiniteScroll();
            if (infiniteScroll) {
                vt.p.setPage(1);
            }
            final List<?> ls = vt.objectsList();
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
                if (infiniteScroll) {
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
                    final String id = vt.idFrom(o);
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

        protected Set<String> getSelectedIds() {
            return selectedIds;
        }

        @Override
        protected void bubble_event(final xwriter js, final a from, final Object o) throws Throwable {
            // event bubbled from child
            if (from instanceof Checkbox && ((vt.enabledViewBits & View.BIT_SELECT) != 0 || vt.isSelectModeMulti())) {
                final String id = ((Checkbox) from).value();
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
            renderRows(x, vt.objectsList());
            y.xube();
        }

    }

}
