package bob.app;

import java.util.ArrayList;
import java.util.List;

import b.a;
import b.xwriter;
import bob.Action;
import bob.Form;

public class form_mock extends Form {
	private static final long serialVersionUID = 1L;

//	public final static class action_mock extends action {
//		private static final long serialVersionUID = 1L;
//
//		public action_mock() {
//			super("alert me");
//		}
//	}

	public a title;

	public form_mock(final String parent_id, final String object_id, final String init_str) {
		super(parent_id, object_id, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		title.set(object_id == null ? init_str : object_id);
	}

	public String getTitle() {
		return b.b.isempty(title.str(), "New mock file");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.p("title: ").inptxt(title, this, "sc");
		x.script().xfocus(title).script_();
		x.nl();
	}

	@Override
	protected List<Action> getActionsList() {
		final List<Action> ls = new ArrayList<Action>();
//		ls.add(new action_mock());
		ls.add(new Action("alert me", "alert"));
		return ls;
	}

	@Override
	protected void onAction(final xwriter x, final Action act) {
		if ("alert".equals(act.code())) {
			x.xalert("alert");
			return;
		}
		super.onAction(x, act);
	}

	@Override
	protected void save(final xwriter x) throws Throwable {
		if (objectId == null) { // create new
			DataMock.ls.add(title.str());
			objectId = title.str();
			return;
		}
		// edit
		for (int i = 0; i < DataMock.ls.size(); i++) {
			final String s = DataMock.ls.get(i);
			if (s.equals(objectId)) {
				DataMock.ls.set(i, title.str());
				objectId = title.str();
				return;
			}
		}
		throw new RuntimeException("could not find " + objectId);
	}

	public final void x_sc(final xwriter x, final String param) throws Throwable {
		saveAndClose(x);
	}
}
