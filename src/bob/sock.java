package bob;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import b.a;
import b.b;
import b.websock;
import b.xwriter;

final public class sock extends websock{
	private final static String axfld="$";
	public String root_class_name="bob.root";
	protected a root;

	public sock(){
		super(true);
	}

	final @Override protected void on_opened() throws Throwable{
//		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_opened");

		// todo load root from db or create new
		root=(a)Class.forName(root_class_name).getConstructor().newInstance();

		final xwriter xjs=new xwriter();
		xwriter x=xjs.xub(root,true,false);
		root.to(x);
		xjs.xube();
		
		send(xjs.toString());
	}

	final @Override protected void on_message(ByteBuffer bb) throws Throwable{
//		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_message: "+bb.remaining()+" bytes");
//		System.out.println(new String(bb.array(),bb.position(),bb.remaining()));
//		System.out.println("-- - -- ------- -- - - - - -- - -");

		final HashMap<String,String> content=populate_content_map_from_buffer(bb);
//		System.out.println(content);

		// ajax post
		String ajax_command_string="";
		for(final Map.Entry<String,String> me:content.entrySet()){
			if(axfld.equals(me.getKey())){
				ajax_command_string=me.getValue();
				continue;
			}
			// ? indexofloop
			final String[] paths=me.getKey().split(a.id_path_separator);
			a e=root;
			for(int n=1;n<paths.length;n++){
				e=e.child(paths[n]);
				if(e==null)
					throw new RuntimeException("not found: "+me.getKey());
			}
			e.set(me.getValue());
		}
		if(ajax_command_string.length()==0)
			throw new RuntimeException("expectedax");

		// decode the field id, method name and parameters parameters
		final String target_elem_id,target_elem_method,target_elem_method_args;
		final int i1=ajax_command_string.indexOf(' ');
		if(i1==-1){
			target_elem_id=ajax_command_string;
			target_elem_method=target_elem_method_args="";
		}else{
			target_elem_id=ajax_command_string.substring(0,i1);
			final int i2=ajax_command_string.indexOf(' ',i1+1);
			if(i2==-1){
				target_elem_method=ajax_command_string.substring(i1+1);
				target_elem_method_args="";
			}else{
				target_elem_method=ajax_command_string.substring(i1+1,i2);
				target_elem_method_args=ajax_command_string.substring(i2+1);
			}
		}
		// navigate to the target element
		final String[] path=target_elem_id.split(a.id_path_separator);// ? indexofloop
		a target_elem=root;
		for(int n=1;n<path.length;n++){
			target_elem=target_elem.child(path[n]);
			if(target_elem==null)
				break;
		}

		final xwriter x=new xwriter();
		if(target_elem==null){
			x.xalert("element not found:\n"+target_elem_id);
			x.finish();
			final String msg=x.toString();
			send(msg);
			return;
		}
		// invoke method on target element with arguments
		try{
			target_elem.getClass().getMethod("x_"+target_elem_method,xwriter.class,String.class).invoke(target_elem,x,target_elem_method_args);
		}catch(final InvocationTargetException t){
			b.log(t.getTargetException());
			x.xalert(b.isempty(t.getTargetException().getMessage(),t.toString()));
		}catch(NoSuchMethodException t){
			x.xalert("method not found:\n"+target_elem.getClass().getName()+".x_"+target_elem_method+"(xwriter,String)");
		}
		x.finish();

		final String msg=x.toString();
//		System.out.println(msg);
		send(msg);
	}

	final @Override protected void on_closed() throws Throwable{
//		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_closed");
		// todo store root in db
	}

	private static HashMap<String,String> populate_content_map_from_buffer(final ByteBuffer content_bb) throws Throwable{
		final HashMap<String,String> content=new HashMap<String,String>();
		byte[] ba=content_bb.array();
		int i=content_bb.position();
		int n=content_bb.limit();
//		System.out.println(new String(ba,i,n-i));
		String name="";
		int s=0;
		int j=i;
		int ba_i=i;
		while(true){
			if(i==n)
				break;
			final byte c=ba[ba_i];
			switch(s){
			default:
				throw new RuntimeException();
			case 0:
				if(c=='='){
					name=new String(ba,i,(j-i),b.strenc);
					i=j+1;
					s=1;
				}
				break;

			case 1:
				if(c=='\r'){
					final String value=new String(ba,i,(j-i),b.strenc);
					content.put(name,value);
					i=j+1;
					s=0;
				}
				break;
			}
			ba_i++;
			j++;
		}
		return content;
	}
}
