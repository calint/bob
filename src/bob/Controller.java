//
// reviewed: 2024-08-05
//           2025-04-28
//           2025-05-02
//
package bob;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import b.a;
import b.req;
import b.xwriter;

/** Controls event flows to the shell. */
public class Controller extends a {

    private final static long serialVersionUID = 1;

    public Menu m;
    public Breadcrumbs bc;
    public a ae; // active element
    public a s; // serialized size
    public a sg; // serialized size, gziped
    public a si; // server info

    public Controller() {
        updateSerializedSize();
        updateServerInfo();
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        x.divh(m, "m").nl(); // menu
        x.divh(bc, "bc").nl(); // breadcrumbs
        final a active_elem = bc.active();
        active_elem.replace(ae);
        x.divh(ae, "ae").nl(); // active element

        // info about size of object
        x.tago("div").attr("class", "ser").tagoe();
        x.p("serialized: ").span(s).p(" B,  gziped: ").span(sg).p(" B, server: ").span(si).spc().ax(this, "s",
                "refresh");
        x.div_().nl();
    }

    @Override
    protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (from == m) {
            // event from the menu, `o` is a class that extends `b.a`
            @SuppressWarnings("unchecked")
            final a e = ((Class<? extends a>) o).getConstructor().newInstance();
            // if instance of view or form complete initialization by calling init
            if (e instanceof View) {
                ((View) e).init();
            } else if (e instanceof Form) {
                ((Form) e).init();
            }
            bc.clear();
            bc.add(e); // add to bread crumb
            e.replace(ae); // replace active element
            x.xu(ae); // update active element
            x.xu(bc); // update bread crumbs
            x.xscroll_to_top();
            return;

        }
        if (from == bc) {
            // event from bread crumbs
            final a e = bc.active();
            e.replace(ae); // replace active element
            x.xu(ae); // update active element
            x.xscroll_to_top();
            return;
        }
        if (from instanceof Form) {
            // event from a form
            if ("close".equals(o)) {
                if (bc.elementCount() > 1) {
                    bc.pop(); // remove last element in bread crumbs
                    final a e = bc.active(); // get current element
                    e.replace(ae); // replace active element
                    x.xu(ae); // update active element
                    x.xu(bc); // update bread crumbs
                    x.xscroll_to_top();
                }
                return;
            }
            if ("updated".equals(o)) {
                x.xu(bc); // update bread crumbs
                return;
            }
        }
        if (from instanceof View && "close".equals(o)) {
            bc.pop(); // remove last element in bread crumbs
            final a e = bc.active(); // get current element
            e.replace(ae); // replace active element
            x.xu(ae); // update active element
            x.xu(bc); // update bread crumbs
            x.xscroll_to_top();
            return;
        }
        if (o instanceof Form || o instanceof View) {
            // open form or view
            final a e = (a) o;
            bc.add(e); // add to bread crumb
            e.replace(ae); // replace active element
            x.xu(ae); // update active element
            x.xu(bc); // update bread crumbs
            x.xscroll_to_top();
            return;
        }
        // event unknown by this element
        super.bubble_event(x, from, o);
    }

    /** Update serialized and server info. */
    public void x_s(final xwriter x, final String param) throws Throwable {
        updateSerializedSize();
        updateServerInfo();
        x.xu(s);
        x.xu(sg);
        x.xu(si);
    }

    private void updateSerializedSize() {
        final byte[] ba = serialize(this);
        s.set(Integer.toString(ba.length));
        sg.set(Integer.toString(gzip(ba).length));
    }

    private void updateServerInfo() {
        si.set(req.get().ip().getHostAddress());
    }

    public static byte[] serialize(final Object o) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(o);
            oos.close();
            return bos.toByteArray();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static byte[] gzip(final byte[] ba) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(ba.length);
        GZIPOutputStream gos = null;
        try {
            gos = new GZIPOutputStream(bos);
            gos.write(ba, 0, ba.length);
            gos.finish();
            gos.flush();
            bos.flush();
            gos.close();
            bos.close();
            return bos.toByteArray();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
