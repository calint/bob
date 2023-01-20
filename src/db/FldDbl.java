package db;

/** Double field. */
public final class FldDbl extends DbField {
	final private Double defval;

	public FldDbl(final double def) {
		super("double", 0, defValToStr(def), false, false);
		defval = def;
	}

	public FldDbl() {
		this(0.0);
	}

	@Override
	protected Object getDefaultValue() {
		return defval;
	}

	// mysql default values returns no decimals if none necessary
	private static String defValToStr(final double def) {
		String s = Double.toString(def);
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}
}
