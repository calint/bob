package bob.elem;

import java.util.ArrayList;
import java.util.List;

import b.a;
import b.xwriter;
import bob.action;
import bob.form;

public class form_mock extends form {
	private static final long serialVersionUID = 1L;

//	public final static class action_mock extends action {
//		private static final long serialVersionUID = 1L;
//
//		public action_mock() {
//			super("alert me");
//		}
//	}

	public a title;

	public form_mock(String pid, String oid, String init) {
		super(pid, oid);
		title.set(oid == null ? init : oid);
	}

	public String getTitle() {
		return b.b.isempty(title.str(), "New mock file");
	}

	@Override
	protected void render(xwriter x) throws Throwable {
		x.p("title: ").inptxt(title);
		x.is().xfocus(title).is_();
	}

	@Override
	protected List<action> getActionsList() {
		List<action> ls = new ArrayList<action>();
//		ls.add(new action_mock());
		ls.add(new action("alert me","alert"));
		return ls;
	}

	@Override
	protected void onAction(xwriter x, action act) {
		if ("alert".equals(act.code())) {
			x.xalert("alert");
			return;
		}
		super.onAction(x, act);
	}

	@Override
	protected void save(xwriter x) throws Throwable {
		if (object_id == null) { // create new
			data.ls.add(title.str());
			object_id = title.str();
			return;
		}
		// edit
		for (int i = 0; i < data.ls.size(); i++) {
			final String s = data.ls.get(i);
			if (s.equals(object_id)) {
				data.ls.set(i, title.str());
				object_id = title.str();
				return;
			}
		}
		throw new RuntimeException("could not find " + object_id);
	}
}
