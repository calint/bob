// reviewed: 2024-08-05
package b;

import static b.b.tobytes;
import static b.b.tostr;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

/** A HTML element. */
public class a implements Serializable {
    public static final String id_path_separator = "-";
    private a parent;
    private String name;
    private String value;

    public a() {
        autonew();
    }

    public a(final a parent, final String name) {
        this.parent = parent;
        this.name = name;
        autonew();
    }

    public a(final a parent, final String name, final String value) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        autonew();
    }

    private void autonew() {
        try {
            if (b.firewall_on) {
                b.firewall_assert_access(this);
            }
            for (final Field f : getClass().getFields()) {
                if (!a.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                a e = (a) f.getType().getConstructor().newInstance();
                e.name = f.getName();
                e.parent = this;
                f.set(this, e);
            }
        } catch (final Throwable e) {
            throw new Error(e);
        }
    }

    public final String id() {
        String s = name;
        for (a p = this; p.parent != null; p = p.parent) {
            s = tostr(p.parent.name, "") + a.id_path_separator + s;
        }
        return tostr(s, a.id_path_separator);
    }

    public final String name() {
        return name;
    }

    public final a parent() {
        return parent;
    }

    /**
     * Override if element contains children that are not defined in fields.
     * 
     * @param id id of element in context
     * @return the element with id
     */
    public a child(final String id) {
        try {
            return (a) getClass().getField(id).get(this);
        } catch (final Throwable ignored) {
        }
        return null;
    }

    /** Bubbles event to parent. Override this to receive events from children. */
    protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (parent != null) {
            parent.bubble_event(x, from, o);
        }
    }

    protected final void bubble_event(final xwriter x, final a from) throws Throwable {
        bubble_event(x, from, null);
    }

    protected final void bubble_event(final xwriter x) throws Throwable {
        bubble_event(x, this, null);
    }

    public void to(final xwriter x) throws Throwable {
        if (value == null) {
            return;
        }
        x.p(value);
    }

    public final a set(final String s) {
        value = s;
        return this;
    }

    public final a set(final a e) {
        value = e.toString();
        return this;
    }

    public final a set(final int i) {
        value = Integer.toString(i);
        return this;
    }

    public final a set(final float f) {
        value = Float.toString(f);
        return this;
    }

    public final a set(final long i) {
        value = Long.toString(i);
        return this;
    }

    public final a set(final double d) {
        value = Double.toString(d);
        return this;
    }

    public final boolean is_empty() {
        return value == null || value.length() == 0;
    }

    @Override
    public String toString() {
        return value == null ? "" : value;
    }

    public final String str() {
        return toString();
    }

    public final int toint() {
        return is_empty() ? 0 : Integer.parseInt(toString());
    }

    public final float toflt() {
        return is_empty() ? 0 : Float.parseFloat(toString());
    }

    public final long tolong() {
        return is_empty() ? 0 : Long.parseLong(toString());
    }

    public final short toshort() {
        return is_empty() ? 0 : Short.parseShort(toString());
    }

    public final double todbl() {
        return is_empty() ? 0 : Double.parseDouble(toString());
    }

    public final void to(final OutputStream os) throws IOException {
        os.write(tobytes(tostr(value, "")));
    }

    public final a parent(final a e) {
        parent = e;
        // ? if parent on_detach(a)?
        return this;
    }

    public final a name(final String s) {
        name = s;
        return this;
    }

    /**
     * Element will not initiate DbTransaction or read and write the state to the
     * session object.
     */
    public static @Retention(RetentionPolicy.RUNTIME) @interface stateless {
    }

    private static final long serialVersionUID = 1;

    /**
     * Replaces the element 'element_to_replace' with this by setting parent and
     * name of the replaced element.
     *
     * @param element_to_replace the element to replace which must be a public
     *                           field.
     */
    public void replace(final a element_to_replace) {
        parent = element_to_replace.parent;
        name = element_to_replace.name;
        try {
            parent.getClass().getField(name).set(parent, this);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
