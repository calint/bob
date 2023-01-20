package db;

import java.sql.Timestamp;

/** Date time field. */
public class FldDateTime extends DbField {
	final private Timestamp defval;

	public FldDateTime() {
		this(null);
	}

	public FldDateTime(final Timestamp def) {
		super("datetime", 0, def == null ? null : FldTs.defValToStr(def), true, true);
		defval = def;
	}

	@Override
	protected Object getDefaultValue() {
		return defval;
	}

}
