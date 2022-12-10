package jem;

import db.DbRelation;

public abstract class ElemRel extends JavaCodeElem {
	final protected DbRelation rel;

	public ElemRel(final DbRelation rel) {
		this.rel = rel;
	}

	protected String getAccessorName() {
		final String nm = rel.getName();
		final StringBuilder sb = new StringBuilder();
		sb.append(nm);
		final char firstChar = sb.charAt(0);
		final char upperChar = Character.toUpperCase(firstChar);
		sb.setCharAt(0, upperChar);
		return sb.toString();
	}
}
