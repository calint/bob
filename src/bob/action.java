package bob;

import java.util.Set;
import b.a;
import b.xwriter;

public abstract class action extends a{
	private static final long serialVersionUID=1L;
	private final String name;
	public action(String name){
		this.name=name;
	}
	public final String getName() {
		return name;
	}
	@Override public final void to(xwriter x) throws Throwable{
		x.ax(this,null,":: "+name);
	}
	public final void x_(xwriter x,String param) throws Throwable{
		bubble_event(x);
	}
	protected abstract void process(a from,Set<String> selectedIds) throws Throwable;
}
