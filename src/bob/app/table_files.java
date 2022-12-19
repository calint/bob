package bob.app;

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

	private String formatDateTime(final long ms) {
		if (sdf == null) {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		return sdf.format(ms);
	}

	public table_files() {
		this(b.b.path());
	}

	public table_files(final path pth) {
		super(BIT_SEARCH, BIT_CLICK_ITEM);
		this.pth = pth;
	}

	public String getTitle() {
		final String nm = pth.name();
		if (".".equals(nm))
			return pth.fullpath();
		return nm;
	}

//	@Override
//	protected TypeInfo getTypeInfo() {
//		return new TypeInfo("file", "files");
//	}
//
//	@Override
//	protected int getObjectsPerPageCount() {
//		return 20;
//	}
//
//	@Override
//	protected int getObjectsCount() {
//		return getResultList().size();
//	}

	@Override
	protected List<?> getObjectsList() {
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
				result.add("./" + pthnm);
			} else {
				result.add(pthnm);
			}
		}
		return result;
	}

	@Override
	protected String getIdFrom(final Object o) {
		return getNameFrom(o);
	}

	@Override
	protected String getNameFrom(final Object o) {
		String dispnm = o.toString();
		if (dispnm.startsWith("./")) {
			dispnm = dispnm.substring("./".length());
		}
		return dispnm;
	}

	@Override
	protected void onAction(final xwriter x, final action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().th().p("Name").th().p("Last modified").th().p("Size");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
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
	protected void onRowClick(final xwriter x, final String id) throws Throwable {
		final path p = pth.get(id);
		if (p.isdir()) {
			final table_files f = new table_files(p);
			super.bubble_event(x, this, f);
			return;
		}
		final form_file f = new form_file(p);
		super.bubble_event(x, this, f);
	}
}
