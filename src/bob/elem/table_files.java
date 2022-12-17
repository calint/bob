package bob.elem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import b.path;
import b.xwriter;
import bob.action;
import bob.util;
import bob.view_table;

public class table_files extends view_table {
	static final long serialVersionUID = 1;

	public static String icon_file_uri = "/bob/file.png";
	public static String icon_folder_uri = "/bob/folder.png";

	private final path pth;
	private transient SimpleDateFormat sdf; // serialized is ~30 KB

	private String formatDateTime(long ms) {
		if (sdf == null)
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(ms);
	}

	public table_files() {
		this(b.b.path());
	}

	public table_files(path pth) {
		super(BIT_SEARCH, BIT_CLICK_ITEM);
		this.pth = pth;
	}

	public String getTitle() {
		final String nm = pth.name();
		if (".".equals(nm)) {
			return pth.fullpath();
		}
		return nm;
	}

	@Override
	protected List<?> getObjectsList() {
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
		for (String pthnm : pth.list()) {
			if (do_query) {
				if (!pattern.matcher(pthnm).find())
					continue;
			}
			path p = pth.get(pthnm);
			if (p.isdir()) {
				result.add("./" + pthnm);
			} else {
				result.add(pthnm);
			}
		}
		Collections.sort(result);
		return result;
	}

	@Override
	protected String getIdFrom(Object o) {
		return getNameFrom(o);
	}

	@Override
	protected String getNameFrom(Object o) {
		String dispnm = o.toString();
		if (dispnm.startsWith("./")) {
			dispnm = dispnm.substring("./".length());
		}
		return dispnm;
	}

	@Override
	protected void onAction(xwriter x, action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}

	@Override
	protected void renderHeaders(xwriter x) {
		x.th().th().p("Name").th().p("Last modified").th().p("Size");
	}

	@Override
	protected void renderRowCells(xwriter x, Object o) {
		final path p = pth.get(o.toString());
		x.td();
		final String img;
		if (p.isdir()) {
			img = "<img src=" + icon_folder_uri + ">";
		} else {
			img = "<img src=" + icon_file_uri + ">";
		}
		renderLinked(x, o, img);
		x.td();
		x.p(getNameFrom(o));
//		renderLinkedName(x, o);
		x.td().p(formatDateTime(p.lastmod()));
		x.td(null, "text-align:right").p(util.formatSizeInBytes(p.size()));
	}

	@Override
	protected void onRowClick(xwriter x, String id) throws Throwable {
		final path p = pth.get(id);
		if (p.isdir()) {
			table_files f = new table_files(p);
			super.bubble_event(x, this, f);
			return;
		}
		form_file f = new form_file(p);
		super.bubble_event(x, this, f);
	}
}
