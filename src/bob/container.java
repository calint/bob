package bob;

import java.util.ArrayList;
import java.util.List;
import b.a;

/** Contains elements in a list. Used to create a name space for dynamically created elements. */
public final class container extends a{
	private static final long serialVersionUID=1L;
	private ArrayList<a> elements=new ArrayList<a>();

	public void clear(){
		elements.clear();
	}
	public void add(a e){
		e.parent(this);
		e.name(Integer.toString(elements.size()));
		elements.add(e);
	}

	/** @return read-only list of elements. To add element use add(...). */
	public List<a> elements(){
		return elements;
	}
	@Override protected a find_child(String nm){
		return elements.get(Integer.parseInt(nm));
	}
}
