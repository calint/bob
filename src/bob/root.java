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
		x.div_();
		x.nl();
		x.p("serialized: ");
//		x.spanot(s,"bytes","background:yellow").p(" onclick=\"").js_x(this,"s \"'hello'\"",true).p("\"").tagoe().p(s.str()).span_();
//		x.spano(s).p(s.str()).span_();
		x.span(s);
		x.p(" B  gziped: ");
		x.span(sg);
		x.p(" B ");
//		x.ax(this,"s \"'hello'\"",":: refresh \"''\"");
		x.ax(this,"s",":: refresh");
		x.nl();
		x.p("server: ").span(si);
//		x.iso().js_x(this,"s \"'hello'\"",false).isc();
//		x.divot(s,"bytes","background:yellow").p(" onclick=\"ui.alert('&quot;\\'hello\\'&quot;')").p("\"").tagoe().p(s).div_();
//		x.divot(s,"bytes","background:yellow").p(s).div_();
//		x.nl().inp(test,"text","txt","background:yellow",this,"test \"'hello'\"","default \"''\"",this,"test ' \"chg\" '");
//		x.nl().inp(test,"text","txt","background:yellow",this,"test \"'hello'\"","default \"''\"",this,null);
//		x.nl().inp(test,"text","txt","background:yellow",this,"test \"'hello'\"","default \"''\"",null,null);
//		x.nl().inp(test,"text","txt","background:yellow",this,"test \"'hello'\"",null,null,null);
//		x.nl().inp(test,"text","txt","background:yellow",this,null,null,null,null);
//		x.nl().inp(test,"text","txt","background:yellow",null,null,null,null,null);
//		x.nl().inp(test,"text","txt",null,null,null,null,null,null);
//		x.nl().inp(test,"text",null,null,null,null,null,null,null);
//		x.nl().inp(test,null,null,null,null,null,null,null,null);
//		x.nl().inp(test,"checkbox",null,null,null,null,null,null,null);
//		x.nl().inp(test,"checkbox",null,null,null,null,null,this,"test");
//		x.nl().inp(test,"checkbox",null,null,null,null,null,this,null);
//		x.nl().inp(test,"checkbox",null,null,this,"test \"'test param'\"",null,null,null);
//		x.nl().inp(test,"checkbox",null,null,this,null,null,null,null);
//		x.nl().inp(test,"date",null,null,this,null,null,null,null);
//		x.nl().inp(test,"time",null,null,null,null,null,null,null);
//		x.nl().inp(test,"number",null,null,null,null,null,null,null);
//		x.nl().inp(test,"color",null,null,null,null,null,null,null);
//		x.nl().inp(test,"image",null,null,null,null,null,null,null);
//		x.nl().inp(test,"file",null,null,null,null,null,null,null);
//		x.nl().inp(test,"search",null,null,null,null,null,null,null);
//		x.nl().inptxtarea(test,"txtarea");
//		x.nl().inptxtarea(test,null);
	}

	public void x_s(xwriter x,String param) throws Throwable{
		System.out.println("*** param:{"+param+"}");
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
