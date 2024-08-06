package jem;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import db.DbObject;

/**
 * Generates accessors for a java class that can be copied and pasted into the
 * source.
 */
public final class Main {

    @SuppressWarnings("unchecked")
    public static void main(final String clsName, final OutputStream os) throws Throwable {
        final JavaCodeEmitter jce = new JavaCodeEmitter();
        final PrintWriter out = new PrintWriter(new OutputStreamWriter(os));// ? param to call
        final Class<? extends DbObject> cls = (Class<? extends DbObject>) Class.forName(clsName);
        jce.emit(out, cls);
    }

}
