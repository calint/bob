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
	private a pt;
	private String nm;
	private String s;
//	public boolean equals(final Object o){
//		if(!(o instanceof a))
//			return false;
//		final a a=(a)o;
//		if(a.pt!=pt)return false;
//		if(a.nm!=null&&!a.nm.equals(nm))return false;
//		if(a.s!=null&&!a.s.equals(s))return false;
//		return true;
//	}
	public a(){autonew();}
	public a(final a parent,final String name){pt=parent;nm=name;autonew();}
	public a(final a parent,final String name,final String value){pt=parent;nm=name;s=value;autonew();}
	private void autonew(){try{
		if(b.firewall_on)b.firewall_assert_access(this);
		if(b.acl_on)b.acl_ensure_create(this);
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
			a.nm=f.getName();
			a.pt=this;				
		}
	}catch(final Throwable e){throw new Error(e);}}
	public final String id(){
		String s=nm;
		for(a p=this;p.pt!=null;p=p.pt)
			s=tostr(p.pt.nm,"")+req.field_path_separator+s;
		return tostr(s,req.field_path_separator);
	}
	public final String nm(){return nm;}
//	public final a nm(final String nm){this.nm=nm;return this;}
	public final a pt(){return pt;}
	public final a pt(final Class<? extends a>cls){
		if(pt==null)return null;
		if(cls.isAssignableFrom(pt.getClass()))return pt;
		return pt.pt(cls);
	}
//	public final a pt(final a a){pt=a;return this;}
	public final void attach(final a e,final String fld){e.pt=this;e.nm=fld;try{getClass().getField(fld).set(this,e);}catch(final Throwable t){throw new Error(t);}}
	final a chld(final String id){try{return (a)getClass().getField(id).get(this);}catch(Throwable e){}return chldq(id);}
	protected a chldq(final String nm){return null;}
	protected void ev(final xwriter x,final a from,final Object o)throws Throwable{if(pt!=null)pt.ev(x,from,o);}
	final protected void ev(final xwriter x,final a from)throws Throwable{ev(x,from,null);}
	final protected void ev(final xwriter x)throws Throwable{ev(x,this,null);}

	
	public void to(final xwriter x)throws Throwable{if(s==null)return;x.p(s);}
	public final a set(final String s){this.s=s;return this;}
	public final a set(final a e){this.s=e.toString();return this;}
	public final a set(final int i){s=Integer.toString(i);return this;}
	public final a set(final float f){s=Float.toString(f);return this;}
	public final a set(final long i){s=Long.toString(i);return this;}
	public final a set(final double d){s=Double.toString(d);return this;}
	public final a clr(){return set((String)null);}
	public final boolean isempty(){return s==null||s.length()==0;}
	public String toString(){return s==null?"":s;}
	public final String str(){return s==null?"":s;}
	public final int toint(){return isempty()?0:Integer.parseInt(toString());}
	public final float toflt(){return isempty()?0:Float.parseFloat(toString());}
	public final long tolong(){return isempty()?0:Long.parseLong(toString());}	
	public final short toshort(){return isempty()?0:Short.parseShort(toString());}
	public final double todbl(){return isempty()?0:Double.parseDouble(toString());}
	
	
	
	
	final public void to(final OutputStream os)throws IOException{os.write(tobytes(tostr(s,"")));}//? impl s?.to(os)
	final public void to(final path p,final boolean append)throws IOException{final OutputStream os=p.outputstream(append);to(os);os.close();}
	final public void to(final path p)throws IOException{to(p,false);}
	final public a from(final path p)throws IOException{//? impl
		final ByteArrayOutputStream baos=new ByteArrayOutputStream((int)p.size());
		p.to(baos);
		baos.close();
		set(baos.toString(strenc));
		return this;
	}
	final public a from(final InputStream in)throws Throwable{
		final InputStreamReader isr=new InputStreamReader(in,b.strenc);
		final StringWriter sw=new StringWriter();
		b.cp(isr,sw,null);
		final String s=sw.toString();
		set(s);
		return this;
	}
	final public a from(final InputStream in,final String defaultIfError){
		try{from(in);}catch(final Throwable t){set(defaultIfError);}
		return this;
	}
	public a pt(final a e){pt=e;return this;}//? if pt ondetach?
	public a nm(final String s){nm=s;return this;}
	final public void xrfsh(final xwriter x)throws Throwable{to(x.xub(this,true,false));x.xube();} 
	/**implement to provide custom html document title*/
//	public interface titled{void title_to(xwriter x);}
	final public Reader reader(){return new StringReader(s==null?"":s);}
	
	private static final long serialVersionUID=1;
}
