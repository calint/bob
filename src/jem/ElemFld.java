package jem;

import db.DbField;

public abstract class ElemFld extends JavaCodeElem {
	final protected DbField fld;

	public ElemFld(final DbField fld) {
		this.fld = fld;
	}

	protected String getAccessorName() {
		final String fldName = fld.getName();
		final StringBuilder sb = new StringBuilder();
		sb.append(fldName);
		final char firstChar = sb.charAt(0);
		final char upperChar = Character.toUpperCase(firstChar);
		sb.setCharAt(0, upperChar);
		return sb.toString();
	}
}
