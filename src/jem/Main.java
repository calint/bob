package jem;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import db.Db;
import db.DbObject;

/**
 * Generates accessors for a java class that can be copied and pasted into the
 * source.
 */
public final class Main {
	@SuppressWarnings("unchecked")
	public final static void main(final String clsName) throws Throwable {
		final JavaCodeEmitter jce = new JavaCodeEmitter();
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));// ? param to call
		final Class<? extends DbObject> cls = (Class<? extends DbObject>) Class.forName(clsName);
		jce.emit(out, cls);
	}
}
