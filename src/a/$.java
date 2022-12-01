package a;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import b.a;
import b.a_ajaxsts;
import b.b;
import b.req;
import b.xwriter;
import db.Db;
import db.test.Book;

public class $ extends a {
	static final long serialVersionUID = 3;
	private int counter = 0;
	public a txt;
	public a_ajaxsts ajaxsts;
//	public diro d;

	public $() {
//		d.root(req.get().session().path());
//		d.bits_set(diro.BIT_ALLOW_FILE_CREATE);
//		d.bits_set(diro.BIT_ALLOW_DIR_CREATE);
	}

	public void to(final xwriter x) throws Throwable {
		x.style().css("body", "padding:0 10em 0 4em").style_();
		final int count = Db.currentTransaction().getCount(Book.class, null);
		x.p("sql count: ").p(count).nl();
		x.p(" server id: ").p(b.id).p(" ").p(req.get().ip().toString()).nl();
		x.p("  counter:").p(++counter).nl();
		x.inptxt(txt);
		x.ax(this, "clk", "click me");
		x.pl().pl();
		ajaxsts.to(x);
		x.pl().pl();
		x.pl("ip addresses:");
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface iface = interfaces.nextElement();
			// filters out 127.0.0.1 and inactive interfaces
			if (iface.isLoopback() || !iface.isUp())
				continue;

			Enumeration<InetAddress> addresses = iface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				String ip = addr.getHostAddress();
				x.pl(iface.getDisplayName() + " " + ip);
			}
		}
//		d.to(x);
	}

	public void x_clk(xwriter x, String s) {
		counter += 10;
		x.xreload();
	}
}
