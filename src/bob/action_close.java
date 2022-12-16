package bob;

import java.util.Set;

import b.a;
import b.xwriter;

public class action_close extends action{
	private static final long serialVersionUID=1L;
	
	public action_close(){
		super("close");
	}
	
	@Override protected void process(xwriter x,a from,Set<String> selectedIds) throws Throwable{
		x.xalert("c");
	}
}
