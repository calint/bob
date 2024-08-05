// reviewed: 2024-08-05
package db;

/** Double field. */
public final class FldDbl extends DbField {

	public FldDbl(final double defVal) {
		super("double", 0, defValToStr(defVal), defVal, false, false);
	}

	public FldDbl() {
		this(0.0);
	}

	// mysql default values returns no decimals if none necessary
	private static String defValToStr(final double def) {
		String s = Double.toString(def);
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}

	public void setDbl(final DbObject ths, final double v) {
		setObj(ths, v);
	}

	public double getDbl(final DbObject ths) {
		return (Double) getObj(ths);
	}

}
