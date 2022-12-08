package bob;

import b.a;
import b.xwriter;

public class checkbox extends a{
	static final long serialVersionUID=1;
	final private static String off="◻";
	final private static String on="▣";

	public checkbox(a parent,String name,boolean checked){
		// name might contain characters that need to be escaped
		super(parent,a.escape_html_name(name),checked?on:off);
		set(checked?on:off);
	}
	public void to(xwriter x) throws Throwable{
		x.spano(this);
		if(on.equals(str())){
			x.ax(this,"u",null,on);
		}else{
			x.ax(this,"s",null,off);
		}
		x.span_();
	}
	public void x_s(xwriter x,String param) throws Throwable{
		set(on);
		bubble(x,this,"checked"); // bubble event
		x.xuo(this); // replace outer html element with this id with the output of this element
	}
	public void x_u(xwriter x,String param) throws Throwable{
		set(off);
		bubble(x,this,"unchecked"); // bubble event
		x.xuo(this); // replace outer html element with this id with the output of this element
	}
}
