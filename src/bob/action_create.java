package bob;

import java.util.Set;
import b.a;

public class action_create extends action{
	private static final long serialVersionUID=1L;
	
	public action_create(){
		super("create");
	}
	
	@Override protected void process(a from,Set<String> selectedIds) throws Throwable{
		System.out.println(getClass().getName()+" "+selectedIds);
		table t=(table)from;
		data.ls.add(t.q.str());
	}
}
