package bob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;
import b.a;
import b.b;
import b.req;
import b.xwriter;

public class root extends a{
	static final long serialVersionUID=1;
	public table t;
	public a s; // serialized size
	public a sg; // gziped size
	public a test;
	public a si; // server info
	public root() throws IOException{
		update_serialized_size();
		update_server_info();
	}
	private void update_server_info(){
		si.set(b.id+" "+req.get().ip().toString());
	}
	public void to(final xwriter x) throws Throwable{
		x.style();
		x.css("table.f","margin-left:auto;margin-right:auto;text-align:left");
		x.css("table.f tr:first-child","border:0;border-bottom:1px solid green;border-top:1px solid #070");
		x.css("table.f tr:last-child","border:0;border-bottom:1px solid #040");
		x.css("table.f th","padding:.5em;text-align:left;background:#fefefe;color:black;border-bottom:1px solid green");
		x.css("table.f td","padding:.5em;vertical-align:middle;border-left:1px dotted #ccc;border-bottom:1px dotted #ccc");
		x.css("table.f td:first-child","border-left:0");
		x.css(t.q,"background:yellow;border:1px dotted #555;width:13em;margin-left:1em;padding:.2em");
		x.style_();
		x.nl();
		x.divo(t);
		t.to(x);
		x.div_().nl();
		x.p("serialized: ").span(s).p(" B  gziped: ").span(sg).p(" B ").ax(this,"s",":: refresh").nl();
		x.p("server: ").span(si);
	}

	public void x_s(xwriter x,String param) throws Throwable{
//		System.out.println("*** param:{"+param+"}");
		update_serialized_size();
		x.xu(s,sg);
	}
//	public void x_test(xwriter x,String param) throws Throwable{
//		System.out.println("x_test: param:{"+param+"} value={"+test.str()+"}");
//	}
	private void update_serialized_size() throws IOException{
		final byte[] ba=serialize(this);
		s.set(Integer.toString(ba.length));
		sg.set(Integer.toString(gzip(ba).length));
	}

	private static byte[] serialize(Object o){
		try{
			final ByteArrayOutputStream bos=new ByteArrayOutputStream(256);
			final ObjectOutputStream oos=new ObjectOutputStream(bos);
			oos.writeObject(o);
			oos.close();
			return bos.toByteArray();
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}
	public static byte[] gzip(byte[] ba) throws IOException{
		ByteArrayOutputStream bos=new ByteArrayOutputStream(ba.length);
		GZIPOutputStream gos=null;
		try{
			gos=new GZIPOutputStream(bos);
			gos.write(ba,0,ba.length);
			gos.finish();
			gos.flush();
			bos.flush();
			return bos.toByteArray();
		}finally{
			if(gos!=null)
				gos.close();
			bos.close();
		}
	}
}
