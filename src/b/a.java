package b;
import static b.b.strenc;
import static b.b.tobytes;
import static b.b.tostr;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
public class a implements Serializable{
	private a parent;
	private String name;
	private String value;
//	public boolean equals(final Object o){
//		if(!(o instanceof a))
//			return false;
//		final a a=(a)o;
//		if(a.pt!=pt)return false;
//		if(a.nm!=null&&!a.nm.equals(nm))return false;
//		if(a.s!=null&&!a.s.equals(s))return false;
//		return true;
//	}
	public a(){
		autonew();
	}
	public a(final a parent,final String name){
		this.parent=parent;
		this.name=name;
		autonew();
	}
	public a(final a parent,final String name,final String value){
		this.parent=parent;
		this.name=name;
		this.value=value;
		autonew();
	}
	private void autonew(){
		try{
			if(b.firewall_on)
				b.firewall_assert_access(this);
//		if(b.acl_on)b.acl_ensure_create(this);
			for(final Field f:getClass().getFields()){
				if(!a.class.isAssignableFrom(f.getType()))
					continue;
//			if(f.getName().startsWith("$"))
//				continue;
				a a=(a)f.get(this);
				if(a==null){
					a=(a)f.getType().getConstructor().newInstance();
					f.set(this,a);
				}
				a.name=f.getName();
				a.parent=this;
			}
		}catch(final Throwable e){
			throw new Error(e);
		}
	}
	public final String id(){
		String s=name;
		for(a p=this;p.parent!=null;p=p.parent)
			s=tostr(p.parent.name,"")+req.ajax_field_path_separator+s;
		return tostr(s,req.ajax_field_path_separator);
	}
	public final String name(){
		return name;
	}
//	public final a nm(final String nm){this.nm=nm;return this;}
	public final a parent(){
		return parent;
	}
	public final a parent(final Class<? extends a> cls){
		if(parent==null)
			return null;
		if(cls.isAssignableFrom(parent.getClass()))
			return parent;
		return parent.parent(cls);
	}
//	public final a pt(final a a){pt=a;return this;}
	public final void attach(final a e,final String fld){
		e.parent=this;
		e.name=fld;
		try{
			getClass().getField(fld).set(this,e);
		}catch(final Throwable t){
			throw new Error(t);
		}
	}
	public final a child(final String id){
		try{
			return (a)getClass().getField(id).get(this);
		}catch(Throwable e){
		}
		return find_child(id);
	}
	/** Override this if element contains children that are not defined in fields. */
	protected a find_child(final String nm){
		return null;
	}
	/** Bubbles event to parent. Override this to receive events from children. */
	protected void bubble(final xwriter x,final a from,final Object o) throws Throwable{
		if(parent!=null)
			parent.bubble(x,from,o);
	}
	final protected void bubble(final xwriter x,final a from) throws Throwable{
		bubble(x,from,null);
	}
	final protected void bubble(final xwriter x) throws Throwable{
		bubble(x,this,null);
	}

	public void to(final xwriter x) throws Throwable{
		if(value==null)
			return;
		x.p(value);
	}
	public final a set(final String s){
		this.value=s;
		return this;
	}
	public final a set(final a e){
		this.value=e.toString();
		return this;
	}
	public final a set(final int i){
		value=Integer.toString(i);
		return this;
	}
	public final a set(final float f){
		value=Float.toString(f);
		return this;
	}
	public final a set(final long i){
		value=Long.toString(i);
		return this;
	}
	public final a set(final double d){
		value=Double.toString(d);
		return this;
	}
	public final a clear(){
		return set((String)null);
	}
	public final boolean is_empty(){
		return value==null||value.length()==0;
	}
	public String toString(){
		return value==null?"":value;
	}
	public final String str(){
		return value==null?"":value;
	}
	public final int toint(){
		return is_empty()?0:Integer.parseInt(toString());
	}
	public final float toflt(){
		return is_empty()?0:Float.parseFloat(toString());
	}
	public final long tolong(){
		return is_empty()?0:Long.parseLong(toString());
	}
	public final short toshort(){
		return is_empty()?0:Short.parseShort(toString());
	}
	public final double todbl(){
		return is_empty()?0:Double.parseDouble(toString());
	}

	final public void to(final OutputStream os) throws IOException{
		os.write(tobytes(tostr(value,"")));
	}// ? impl s?.to(os)
	final public void to(final path p,final boolean append) throws IOException{
		final OutputStream os=p.outputstream(append);
		to(os);
		os.close();
	}
	final public void to(final path p) throws IOException{
		to(p,false);
	}
	final public a from(final path p) throws Throwable{// ? impl
		final ByteArrayOutputStream baos=new ByteArrayOutputStream((int)p.size());
		p.to(baos);
		baos.close();
		set(baos.toString(strenc));
		return this;
	}
	final public a from(final InputStream in) throws Throwable{
		final InputStreamReader isr=new InputStreamReader(in,b.strenc);
		final StringWriter sw=new StringWriter();
		b.cp(isr,sw,null);
		final String s=sw.toString();
		set(s);
		return this;
	}
	final public a from(final InputStream in,final String defaultIfError){
		try{
			from(in);
		}catch(final Throwable t){
			set(defaultIfError);
		}
		return this;
	}
	public a parent(final a e){
		parent=e;
		return this;
	}// ? if pt ondetach?
	public a name(final String s){
		name=s;
		return this;
	}
	final public void xrfsh(final xwriter x) throws Throwable{
		to(x.xub(this,true,false));
		x.xube();
	}
	/** implement to provide custom html document title */
//	public interface titled{void title_to(xwriter x);}
	final public Reader reader(){
		return new StringReader(value==null?"":value);
	}
	
	final public static String escape_html_name(String name){
		final String escaped=name.replace('+','§').replace(' ','+');
//		final String escaped=name.replaceAll("\\+","%2b").replace(' ','+'); // ?  cannot replace with %2b because browser unescapes it in links
		return escaped;
	}

	final public static String unescape_html_name(String name){
		final String unescaped=name.replace('+',' ').replace('§','+');
		return unescaped;
	}
	
	private static final long serialVersionUID=1;
}
