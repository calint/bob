//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import b.path;
import b.xwriter;
import bob.Form;
import bob.Util;
import bob.View;
import bob.ViewTable;

/** Navigator of file system. */
public final class TableFsFiles extends ViewTable {

    private final static long serialVersionUID = 1;

    public static String icon_file_uri = "/bob/file.png";
    public static String icon_folder_uri = "/bob/folder.png";

    private final path pth;
    private transient SimpleDateFormat sdf; // note: serialized is ~30 KB

    private String formatDateTime(final long ms) {
        if (sdf == null) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        return sdf.format(ms);
    }

    public TableFsFiles() {
        this(b.b.path());
    }

    public TableFsFiles(final path pth) {
        super(new TypeInfo("file", "files"), null, BIT_SEARCH, BIT_RENDER_LINKED_ITEM);
        this.pth = pth;
    }

    public String title() {
        final String nm = pth.name();
        if (".".equals(nm)) {
            return pth.fullpath();
        }
        return nm;
    }

    @Override
    protected List<?> objectsList() {
        final List<String> result = getResultList();
        Collections.sort(result);
        if (p.isEnabled()) {
            int toIndex = p.getLimitStart() + p.getLimitCount();
            if (toIndex > result.size()) {
                toIndex = result.size();
            }
            return result.subList(p.getLimitStart(), toIndex);
        }
        return result;
    }

    private List<String> getResultList() {
        final boolean do_query = !b.b.isempty(q.str());
        final Pattern pattern;
        final String qstr;
        if (do_query) {
            qstr = q.str().replace("*", ".*");
            pattern = Pattern.compile(qstr, Pattern.CASE_INSENSITIVE);
        } else {
            qstr = "";
            pattern = null;
        }
        final List<String> result = new ArrayList<String>();
        for (final String pthnm : pth.list()) {
            if (do_query && !pattern.matcher(pthnm).find()) {
                continue;
            }
            final path p = pth.get(pthnm);
            if (p.isdir()) {
                // put a prefix so that directories are placed before files by sort
                result.add("./" + pthnm);
            } else {
                result.add(pthnm);
            }
        }
        return result;
    }

    @Override
    protected String idFrom(final Object o) {
        String dispnm = o.toString();
        if (dispnm.startsWith("./")) {
            dispnm = dispnm.substring("./".length());
        }
        return dispnm;
    }

    @Override
    protected void renderHeaders(final xwriter x) {
        x.th().th().p("Name").th().p("Last modified").th().p("Size");
    }

    @Override
    protected void renderRowCells(final xwriter x, final Object o) {
        final path p = pth.get(o.toString());
        x.td("icn");
        final String img;
        if (p.isdir()) {
            img = "<img src=" + icon_folder_uri + ">";
        } else {
            img = "<img src=" + icon_file_uri + ">";
        }
        renderLink(x, o, img); // link `img` to action `onRowClick`
        x.td().p(p.name());
        x.td().p(formatDateTime(p.lastmod()));
        x.td("nbr").p(Util.formatSizeInBytes(p.size()));
    }

    @Override
    protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
        final path p = pth.get(id);
        if (p.isdir()) {
            final View view = new TableFsFiles(p).init();
            super.bubble_event(x, this, view);
            return;
        }
        final Form f = new FormFsFile(p).init();
        super.bubble_event(x, this, f);
    }

}
