package db;

import java.sql.Timestamp;
import java.util.Map;

/** Date time field. */
public class FldDateTime extends DbField {
	private static final long serialVersionUID = 1L;

	final private Timestamp defval;

	public FldDateTime() {
		this(null);
	}

	public FldDateTime(final Timestamp def) {
		super("datetime", 0, def == null ? null : FldTs.defValToStr(def), true, true);
		defval = def;
	}

	@Override
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
		if (defval == null)
			return;

		kvm.put(this, defval);
	}

}
