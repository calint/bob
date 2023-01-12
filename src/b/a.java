package b;

import static b.b.tobytes;
import static b.b.tostr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class a implements Serializable {
	public final static String id_path_separator = "-";
	private a parent;
	private String name;
	private String value;

	public a() {
		autonew();
	}

	public a(final a parent, final String name) {
		this.parent = parent;
		this.name = name;
		autonew();
	}

	public a(final a parent, final String name, final String value) {
		this.parent = parent;
		this.name = name;
		this.value = value;
		autonew();
	}

	private void autonew() {
		try {
			if (b.firewall_on) {
				b.firewall_assert_access(this);
			}
//		if(b.acl_on)b.acl_ensure_create(this);
			for (final Field f : getClass().getFields()) {
				if (!a.class.isAssignableFrom(f.getType())) {
					continue;
				}
//			if(f.getName().startsWith("$"))
//				continue;
				a a = (a) f.get(this);
				if (a == null) {
					a = (a) f.getType().getConstructor().newInstance();
					f.set(this, a);
				}
				a.name = f.getName();
				a.parent = this;
			}
		} catch (final Throwable e) {
			throw new Error(e);
		}
	}

	public final String id() {
		String s = name;
		for (a p = this; p.parent != null; p = p.parent) {
			s = tostr(p.parent.name, "") + a.id_path_separator + s;
		}
		return tostr(s, a.id_path_separator);
	}

	public final String name() {
		return name;
	}

//	public final a nm(final String nm){this.nm=nm;return this;}
	public final a parent() {
		return parent;
	}

//	public final a parent(final Class<? extends a> cls){
//		if(parent==null)
//			return null;
//		if(cls.isAssignableFrom(parent.getClass()))
//			return parent;
//		return parent.parent(cls);
//	}
//	public final a pt(final a a){pt=a;return this;}
//	public final void attach(final a e,final String field_name){
//		e.parent=this;
//		e.name=field_name;
//		try{
//			getClass().getField(field_name).set(this,e);
//		}catch(final Throwable t){
//			throw new Error(t);
//		}
//	}

	/**
	 * Override this if element contains children that are not defined in fields.
	 */
	public a child(final String id) {
		try {
			return (a) getClass().getField(id).get(this);
		} catch (final Throwable ignored) {
		}
		return null;
	}

//	protected a find_child(final String nm) {
//		return null;
//	}

	/** Bubbles event to parent. Override this to receive events from children. */
	protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
		if (parent != null) {
			parent.bubble_event(x, from, o);
		}
	}

	final protected void bubble_event(final xwriter x, final a from) throws Throwable {
		bubble_event(x, from, null);
	}

	final protected void bubble_event(final xwriter x) throws Throwable {
		bubble_event(x, this, null);
	}

	public void to(final xwriter x) throws Throwable {
		if (value == null)
			return;
		x.p(value);
	}

	public final a set(final String s) {
		value = s;
		return this;
	}

	public final a set(final a e) {
		value = e.toString();
		return this;
	}

	public final a set(final int i) {
		value = Integer.toString(i);
		return this;
	}

	public final a set(final float f) {
		value = Float.toString(f);
		return this;
	}

	public final a set(final long i) {
		value = Long.toString(i);
		return this;
	}

	public final a set(final double d) {
		value = Double.toString(d);
		return this;
	}

//	public final a clear(){
//		return set((String)null);
//	}
	public final boolean is_empty() {
		return value == null || value.length() == 0;
	}

	@Override
	public String toString() {
		return value == null ? "" : value;
	}

	public final String str() {
		return value == null ? "" : value;
	}

	public final int toint() {
		return is_empty() ? 0 : Integer.parseInt(toString());
	}

	public final float toflt() {
		return is_empty() ? 0 : Float.parseFloat(toString());
	}

	public final long tolong() {
		return is_empty() ? 0 : Long.parseLong(toString());
	}

	public final short toshort() {
		return is_empty() ? 0 : Short.parseShort(toString());
	}

	public final double todbl() {
		return is_empty() ? 0 : Double.parseDouble(toString());
	}

	final public void to(final OutputStream os) throws IOException {
		os.write(tobytes(tostr(value, "")));
	}
//
//	final public void to(final path p,final boolean append) throws IOException{
//		final OutputStream os=p.outputstream(append);
//		to(os);
//		os.close();
//	}

//	final public void to(final path p) throws IOException{
//		to(p,false);
//	}
//	final public a from(final path p) throws Throwable{// ? impl
//		final ByteArrayOutputStream baos=new ByteArrayOutputStream((int)p.size());
//		p.to(baos);
//		baos.close();
//		set(baos.toString(strenc));
//		return this;
//	}
//	final public a from(final InputStream in) throws Throwable{
//		final InputStreamReader isr=new InputStreamReader(in,b.strenc);
//		final StringWriter sw=new StringWriter();
//		b.cp(isr,sw,null);
//		final String s=sw.toString();
//		set(s);
//		return this;
//	}
//	final public a from(final InputStream in,final String defaultIfError){
//		try{
//			from(in);
//		}catch(final Throwable t){
//			set(defaultIfError);
//		}
//		return this;
//	}
	public final a parent(final a e) {
		parent = e;
		// ? if parent on_detach(a)?
		return this;
	}

	public final a name(final String s) {
		name = s;
		return this;
	}
//	final public void xrfsh(final xwriter x) throws Throwable{
//		to(x.xub(this,true,false));
//		x.xube();
//	}
	/** implement to provide custom html document title */
//	public interface titled{void title_to(xwriter x);}
//	final public Reader reader(){
//		return new StringReader(value==null?"":value);
//	}

//	final public static String html_escape(String name){
//		final String escaped=name.replace('+','§').replace(' ','+');
////		final String escaped=name.replaceAll("\\+","%2b").replace(' ','+'); // ?  cannot replace with %2b because browser unescapes it in links
//		return escaped;
//	}
//
//	final public static String html_unescape(String name){
//		final String unescaped=name.replace('+',' ').replace('§','+');
//		return unescaped;
//	}

	/**
	 * Element will not initiate DbTransaction or read and write the state to the
	 * session object.
	 */
	public static @Retention(RetentionPolicy.RUNTIME) @interface stateless {
	}

	private static final long serialVersionUID = 1;

	/**
	 * Replaces the element element_to_replace with this by setting parent to
	 * new_parent and name to the name of element_to_replace.
	 *
	 * @param new_parent         the new parent of this element.
	 * @param element_to_replace the element to replace which must be a public
	 *                           field.
	 */
	public void replace(final a new_parent, final a element_to_replace) {
		parent = new_parent;
		name = element_to_replace.name;
		try {
			new_parent.getClass().getField(element_to_replace.name).set(new_parent, this);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
