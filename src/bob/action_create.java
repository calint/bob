package bob;

import java.util.Set;

import b.a;
import b.xwriter;

public class action_create extends action{
	private static final long serialVersionUID=1L;
	
	public action_create(){
		super("create");
	}
	
	@Override protected void process(xwriter x,a from,Set<String> selectedIds) throws Throwable{
		System.out.println(getClass().getName()+" "+selectedIds);
		table_view t=(table_view)from;
		data.ls.add(t.q.str());
	}
}
