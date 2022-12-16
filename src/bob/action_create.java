package bob;

public class action_create extends action {
	private static final long serialVersionUID = 1L;
	private final Class<? extends form> form_cls;
	/** Parent id. */
	private final String pid;

	public action_create(String pid, Class<? extends form> form_cls) {
		super("create");
		this.pid = pid;
		this.form_cls = form_cls;
	}

	public form createForm(String initstr) throws Throwable {
		return (form) form_cls.getConstructor(String.class, String.class, String.class).newInstance(pid, null, initstr);
	}
}
