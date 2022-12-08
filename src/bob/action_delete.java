package bob;

import java.util.Set;
import b.a;

public class action_delete extends action{
	private static final long serialVersionUID=1L;

	public action_delete(){
		super("delete");
	}

	@Override protected void process(a from,Set<String> selectedIds) throws Throwable{
		System.out.println(getClass().getName()+" "+selectedIds);
		for(String id:selectedIds){
			data.ls.remove(id);
		}
		selectedIds.clear();
	}
}
