package db;

import java.sql.Timestamp;
import java.util.Map;

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
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
		if (defval == null)
			return;

		kvm.put(this, defval);
	}

	// java.sql.Timestamp adds .0 at the end. mysql default value does not.
	public static String defValToStr(final Timestamp def) {
		String s = def.toString();
		if (s.endsWith(".0"))
			s = s.substring(0, s.length() - 2);
		return s;
	}
}
