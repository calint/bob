package db;

/** Primary key integer field. */
public final class FldId extends DbField {
	private static final long serialVersionUID = 1L;
	
	FldId() {
		super("int", 0, null, false, false);
	}

	@Override
	protected void sql_columnDefinition(final StringBuilder sb) {
		sb.append(name).append(' ').append(getSqlType()).append(" primary key auto_increment");
	}
}
