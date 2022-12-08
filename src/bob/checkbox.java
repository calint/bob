package bob;

import b.a;
import b.xwriter;

public class checkbox extends a{
	static final long serialVersionUID=1;
	final private static String off="◻";
	final private static String on="▣";
	final private String id;
	public checkbox(String id,boolean checked){
		set(checked?on:off);
		this.id=id;
	}
	public String getId(){
		return id;
	}
	public void to(xwriter x) throws Throwable{
		x.spano(this);
		if(on.equals(str())){
			x.ax(this,"u",on);
		}else{
			x.ax(this,"s",off);
		}
		x.span_();
	}
	public void x_s(xwriter x,String param) throws Throwable{
		set(on);
		bubble_event(x,this,"checked"); // bubble event
		x.xuo(this); // replace outer html element with this id with the output of this element
	}
	public void x_u(xwriter x,String param) throws Throwable{
		set(off);
		bubble_event(x,this,"unchecked"); // bubble event
		x.xuo(this); // replace outer html element with this id with the output of this element
	}
}
