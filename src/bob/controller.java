package bob;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import b.a;
import b.b;
import b.req;
import b.xwriter;

public class controller extends a {
	static final long serialVersionUID = 1;
	public a s; // serialized size
	public a sg; // serialized size, gziped
	public a si; // server info
	public bread_crumbs bc;
	public a ae; // active element
//	public a test;

	public controller() {
		update_serialized_size();
		update_server_info();
	}

	private void update_server_info() {
		si.set(b.id + " " + req.get().ip().getHostAddress());
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		x.nl();
		x.divh(bc);
		final a active_elem = bc.getActive();
		active_elem.replace(this, ae);
		x.divh(ae);
		x.nl();
		x.p("serialized: ").span(s).p(" B  gziped: ").span(sg).p(" B ").ax(this, "s", ":: refresh").nl();
		x.p("server: ").span(si);
		x.nl().nl();
	}

	@Override
	protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
		if (from == bc) { // event from bread crumb
			final a e = ((bread_crumbs) from).getActive();
			e.replace(this, ae); // replace active element
			x.xu(ae); // update active element
			return;
		}
		if (from instanceof form) { // event from a form
			if ("close".equals(o)) {
				bc.removeLast(); // remove last element in bread crumbs
				final a e = bc.getActive(); // get current element
				e.replace(this, ae); // replace active element
				x.xu(ae); // update active element
				x.xu(bc); // update bread crumbs
				return;
			}
			if ("updated".equals(o)) {
				x.xu(bc); // update bread crumbs
				return;
			}
		}
		if ((o instanceof form) || (o instanceof view)) { // open view
			final a e = (a) o;
			bc.add(e); // add to bread crumb
			e.replace(this, ae); // replace active element
			x.xu(ae); // update active element
			x.xu(bc); // update bread crumbs
			return;
		}
		// event unknown by this element
		super.bubble_event(x, from, o);
	}

	public void x_s(final xwriter x, final String param) throws Throwable {
//		System.out.println("*** param:{"+param+"}");
		update_serialized_size();
		x.xu(s, sg);
	}

	private void update_serialized_size() {
		final byte[] ba = serialize(this);
		s.set(Integer.toString(ba.length));
		sg.set(Integer.toString(gzip(ba).length));
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

	public void x_test(final xwriter x, final String param) {
		System.out.println("test param{" + param + "}");
	}

	public void x_sel(final xwriter x, final String param) {
		System.out.println("sel param{" + param + "}");
	}

}
