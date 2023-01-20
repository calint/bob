package db;

import java.sql.Timestamp;

/** Timestamp field. */
public final class FldTs extends DbField {
	final private Timestamp defval;

	public FldTs(final Timestamp def) {
		super("timestamp", 0, def == null ? null : defValToStr(def), true, true);
		defval = def;
	}

	public FldTs() {
		this(null);
	}

	@Override
	protected void setDefaultValue(final Object[] values) {
		if (defval == null)
			return;
		values[slotNbr] = defval;
	}

	// java.sql.Timestamp adds .0 at the end. mysql default value does not.
	public static String defValToStr(final Timestamp def) {
		String s = def.toString();
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}
}
