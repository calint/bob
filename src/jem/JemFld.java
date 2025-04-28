//
// reviewed: 2025-04-28
//
package jem;

import db.DbField;

public abstract class JemFld extends JavaCodeElem {

    protected final DbField fld;

    public JemFld(final DbField fld) {
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
