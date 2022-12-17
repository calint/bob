package bob.elem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import b.path;
import b.xwriter;
import bob.action;
import bob.util;
import bob.view_table;

public class table_files extends view_table {
	static final long serialVersionUID = 1;

	private final path pth;
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // ? serialized is ~30 KB

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
		final List<String> result = new ArrayList<String>();
		final String qstr = q.str().toLowerCase();
		for (String pthnm : pth.list()) {
			if (!pthnm.toLowerCase().startsWith(qstr)) {
				continue;
			}
			final path p = pth.get(pthnm);
			if (p.isdir()) {
				result.add("/" + pthnm);
			} else {
				result.add(pthnm);
			}
		}
		Collections.sort(result);
		return result;
	}

	@Override
	protected String getIdFrom(Object o) {
		return o.toString();
	}

	@Override
	protected String getNameFrom(Object o) {
		return o.toString();
	}

	@Override
	protected void onAction(xwriter x, action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}

	@Override
	protected void renderHeaders(xwriter x) {
		x.th().p("Created").th().p("Size");
	}

	@Override
	protected void renderRowCells(xwriter x, Object o) {
		final path p = pth.get(o.toString());
		x.td().p(sdf.format(p.lastmod()));
		x.td(null,"text-align:right").p(util.formatSizeInBytes(p.size()));
	}

	@Override
	protected void onRowClick(xwriter x, String id) throws Throwable {
		final path p = pth.get(id);
		if (p.isdir()) {
			table_files f = new table_files(p);
			super.bubble_event(x, this, f);
			return;
		}
		form_file f=new form_file(p);
		super.bubble_event(x, this, f);
	}
}
