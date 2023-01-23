package db;

/** Float field. */
public final class FldFlt extends DbField {
	private final Float defval;

	public FldFlt(final float def) {
		super("float", 0, defValToStr(def), false, false);
		defval = def;
	}

	public FldFlt() {
		this(0.0f);
	}

	@Override
	protected Object getDefaultValue() {
		return defval;
	}

	// mysql default values returns no decimals if none necessary
	private static String defValToStr(final float def) {
		String s = Float.toString(def);
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}

	public void setFlt(final DbObject ths, final float v) {
		setObj(ths, v);
	}

	public float getFlt(final DbObject ths) {
		return (Float) getObj(ths);
	}

}
