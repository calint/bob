package bob;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import b.a;
import b.xwriter;

public class root extends a{
	static final long serialVersionUID=1;
	public table t;
	public a s;

	public void to(final xwriter x) throws Throwable{
		x.style();
		x.css("table.f","margin-left:auto;margin-right:auto");
		x.css("table.f tr:first-child","border:0;border-bottom:1px solid green;border-top:1px solid #070");
		x.css("table.f tr:last-child","border:0;border-bottom:1px solid #040");
		x.css("table.f th","padding:.5em;text-align:left;background:#fefefe;color:black;border-bottom:1px solid green");
		x.css("table.f td","padding:.5em;vertical-align:middle;border-left:1px dotted #ccc;border-bottom:1px dotted #ccc");
		x.css("table.f td:first-child","border-left:0");
//		x.css(t.q,"float:right;background:yellow;border:1px dotted #555;text-align:right;width:10em;margin-left:1em");
		x.css(t.q,"background:yellow;border:1px dotted #555;width:13em;margin-left:1em;padding:.2em");
		x.style_();
		x.nl();
		x.divo(t);
		t.to(x);
		x.div_();
		x.p("serialized size: ").spano(s).p(serialize(this).length).span_().p(" B ");
		x.ax(this,"s",":: refresh");
	}
	
	public void x_s(xwriter x,String param){
		x.xu(s,Integer.toString(serialize(this).length));
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
}
