// reviewed: 2024-08-05
package db;

/** Float field. */
public final class FldFlt extends DbField {

	public FldFlt(final float defVal) {
		super("float", 0, defValToStr(defVal), defVal, false, false);
	}

	public FldFlt() {
		this(0.0f);
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
