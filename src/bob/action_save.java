package bob;

import java.util.Set;

import b.a;
import b.xwriter;

public class action_save extends action{
	private static final long serialVersionUID=1L;
	
	public action_save(){
		super("save");
	}
	
	@Override protected void process(xwriter x,a from,Set<String> selectedIds) throws Throwable{
		x.xalert("s");
	}
}
