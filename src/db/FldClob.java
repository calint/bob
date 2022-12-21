package db;

/** CLOB field. */
public final class FldClob extends DbField {
	private static final long serialVersionUID = 1L;

	public FldClob() {
		super("longtext", 0, null, true, true);
	}
}
