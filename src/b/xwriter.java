package b;

import static b.b.isempty;
import static b.b.tobytes;
import static b.b.tostr;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/** Low-level printer of HTML and JavaScript stream. */
public final class xwriter {
	public static String enc_js_in_attr(final String text) {
		if (text == null)
			return "";
		return text.replace("'", "\\'").replace("\"", "&quot;");
	}

	public static String enc_js_str(final String text) {
		if (text == null)
			return "";
		return text.replace("'", "\\'");
	}

	public static String enc_quot(final String text) {
		if (text == null)
			return "";
		return text.replace("\"", "&quot;");
	}

	private boolean is_xu_open; // if xu is open then it needs to be closed at error messaging to client
	private final OutputStream os;
	private boolean xreload_requested; // reload page races with element serialization to db

	public xwriter() {
		os = new ByteArrayOutputStream();
	}

	public xwriter(final OutputStream os) {
		this.os = os;
	}

	public xwriter a(final String href) {
		return tago("a").attr("href", enc_quot(href)).tagoe();
	}

	public xwriter a(final String href, final String html) {
		return a(href).p(html).a_();
	}

	public xwriter a_() {
		return tage("a");
	}

	public xwriter attr(final String name) {
		return p(" ").p(name);
	}

	public xwriter attr(final String name, final int value) {
		return p(" ").p(name).p("=").p(value);
	}

	public xwriter attr(final String name, final String value) {
		return p(" ").p(name).p("=\"").p(enc_quot(value)).p("\"");
	}

	public xwriter ax(final a e) {
		return ax(e, "", "::");
	}

	public xwriter ax(final a e, final String callback) {
		return ax(e, callback, callback);
	}

	public xwriter ax(final a e, final String callback, final String html) {
		p("<a href=\"javascript:");
		js_x(e, callback, true);
		p("\">").p(html).p("</a>");
		return this;
	}

	public xwriter bell() {
		return p('\07');
	}

	public xwriter br() {
		return tag("br");
	}

	public xwriter closeUpdateIfOpen() {
		if (!is_xu_open)
			return this;
		is_xu_open = false;
		return xube();
	}

	public xwriter code() {
		return tag("code");
	}

	public xwriter code_() {
		return tage("code");
	}

	public xwriter css(final a e, final String style) {
		return p("#").p(e.id()).p("{").p(style).p("}");
	}

	public xwriter css(final a e, final String selector, final String style) {
		return css("#" + e.id() + " " + selector, style);
	}

	public xwriter css(final String selector, final String style) {
		return p(selector).p("{").p(style).p("}");
	}

	public xwriter cssfont(final String name, final String url) {
		return p("@font-face{font-family:").p(name).p(";src:url(").p(url).p(");}");
	}

	public xwriter default_attrs_for_element(final a e) {
		return default_attrs_for_element(e, null, null);
	}

	public xwriter default_attrs_for_element(final a e, final String cls, final String style) {
		if (e != null) {
			attr("id", e.id());
		}
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		return this;
	}

	public xwriter div(final a e) {
		return div(e, null, null);
	}

	public xwriter div(final a e, final String cls) {
		return div(e, cls, null);
	}

	/** Renders a complete div with HTML escaped element output. */
	public xwriter div(final a e, final String cls, final String style) {
		divo(e, cls, style).tagoe();
		try {
			e.to(new osltgt(os));
		} catch (final Throwable t) {
			throw new Error(t);
		}
		return div_();
	}

	public xwriter div_() {
		return tage("div");
	}

	/** Renders a 'div' tag with unescaped element output assumed to be HTML. */
	public xwriter div_html(final a e, final String cls, final String style) {
		return divh(e, cls, style);
	}

	public xwriter divh(final a e) {
		return divh(e, null, null);
	}

	public xwriter divh(final a e, final String cls) {
		return divh(e, cls, null);
	}

	public xwriter divh(final a e, final String cls, final String style) {
		tago("div").attr("id", e.id());
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		tagoe();
		try {
			e.to(this);
		} catch (final Throwable t) {
			throw new Error(t);
		}
		return div_();
	}

	/**
	 * Opens a 'div' tag so that other attributes can be added. Close with
	 * 'tagoe()'.
	 */
	public xwriter divo(final a e, final String cls, final String style) {
		tago("div").default_attrs_for_element(e, cls, style);
		return this;
	}

	public xwriter enter() {
		return p('\r');
	}

	/**
	 * Called before closing, being the last script added. Used to avoid racing
	 * between DbTransaction.commit() and reloading the page.
	 */
	public void finish() {
		if (xreload_requested) {
			pl("location.href=location.href");
		}
		xreload_requested = false;
	}

	public xwriter flush() {
		try {
			os.flush();
			return this;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public xwriter focus(final a e) {
		return script().p("$f('").p(e.id()).p("')").script_();
	}

	public xwriter hr() {
		return tag("hr");
	}

	// ? review this
	public xwriter inp(final a e, final String type, final String cls, final String style, final String default_value,
			final a on_enter_callback_elem, final String on_enter_callback, final a on_change_callback_elem,
			final String on_change_callback) {
		final String value = e.is_empty() ? default_value : e.str();
		tago("input").attr("value", value).default_attrs_for_element(e, cls, style);
		if (!isempty(type)) {
			attr("type", type);
		}
		if (on_enter_callback_elem != null) {
			final StringBuilder sb = new StringBuilder(64);
			sb.append("return $r(event,this,'");
			sb.append(on_enter_callback_elem.id());
			if (!isempty(on_enter_callback)) {
				sb.append(' ').append(enc_js_in_attr(on_enter_callback));
			}
			sb.append("')");
			attr("onkeypress", sb.toString());
		}
		final StringBuilder sb = new StringBuilder(64);
		if ("checkbox".equals(type)) {
			if ("1".equals(value)) {
				attr("checked");
			}
			sb.append("this.value=this.checked?'1':'0';$b(this)");
			if (on_change_callback_elem != null) {
				sb.append(";$x('");
				sb.append(on_change_callback_elem.id());
				if (!isempty(on_change_callback)) {
					sb.append(' ').append(on_change_callback);
				}
				sb.append("')");
			}
		} else {
			sb.append("$b(this)");
			if (on_change_callback_elem != null) {
				sb.append(";$x('");
				sb.append(on_change_callback_elem.id());
				if (!isempty(on_change_callback)) {
					sb.append(' ');
					sb.append(enc_js_in_attr(on_change_callback));
				}
				sb.append("')");
			}
		}
		attr("oninput", sb.toString());
		return tagoe();
	}

	public xwriter inpflt(final a e) {
		return tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text")
				.attr("class", "nbr").attr("size", 5).attr("oninput", "$b(this)").tagoe();
	}

	public xwriter inpint(final a e) {
		return tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text")
				.attr("class", "nbr").attr("size", 5).attr("oninput", "$b(this)").tagoe();
	}

	public xwriter inplng(final a e) {
		return inpint(e);
	}

	public xwriter inptxt(final a e) {
		return inp(e, null, null, null, null, null, null, null, null);
	}

	public xwriter inptxt(final a e, final a callback_elem_on_enter) {
		return inp(e, null, null, null, null, callback_elem_on_enter, null, null, null);
	}

	public xwriter inptxt(final a e, final a callback_elem_on_enter, final String callback) {
		return inp(e, null, null, null, null, callback_elem_on_enter, callback, null, null);
	}

	public xwriter inptxt(final a e, final String cls) {
		return inp(e, null, cls, null, null, null, null, null, null);
	}

	public xwriter inptxt(final a e, final String cls, final a callback_elem_on_enter, final String callback) {
		return inp(e, null, cls, null, null, callback_elem_on_enter, callback, null, null);
	}

	public xwriter inptxt(final a e, final String cls, final String default_value, final a callback_elem_on_enter,
			final String callback) {
		return inp(e, null, cls, null, default_value, callback_elem_on_enter, callback, null, null);
	}

	public xwriter inptxtarea(final a e) {
		return inptxtarea(e, null, null);
	}

	public xwriter inptxtarea(final a e, final String cls) {
		return inptxtarea(e, cls, null);
	}

	/** Input text area. */
	public xwriter inptxtarea(final a e, final String cls, final String style) {
		tago("textarea").default_attrs_for_element(e, cls, style).attr("oninput", "$b(this)").tagoe();
		try {
			e.to(new osltgt(outputstream()));
		} catch (final Throwable t) {
			b.log(t);
			p(b.stacktrace(t));
		}
		return tage("textarea");
	}

	/**
	 * Renders script for a callback.
	 *
	 * @param e                    element to call
	 * @param callback             first word is method name and remaining string is
	 *                             the parameter
	 * @param encode_for_attribute true to encode parameter for script in HTML
	 *                             attribute
	 */
	public xwriter js_x(final a e, final String callback, final boolean encode_for_attribute) {
		p("$x('").p(e.id());
		if (!isempty(callback)) {
			p(" ");
			if (encode_for_attribute) {
				p(enc_js_in_attr(callback));
			} else {
				p(enc_js_str(callback));
			}
		}
		p("');");
		return this;
	}

	@SuppressWarnings("resource") // osjsstr does not need to do any cleanup
	public xwriter jsstr(final String s) {
		try {
			new osjsstr(os).write(tobytes(s));
			return this;
		} catch (final IOException e) {
			throw new Error(e);
		}
	}

	public xwriter li() {
		return li(null);
	}

	public xwriter li(final String cls) {
		if (isempty(cls))
			return li();
		return tago("li").attr("class", cls).tagoe();
	}

	public xwriter nbsp() {
		return p("&nbsp;");
	}

	public xwriter nl() {
		return p('\n');
	}

	public xwriter ol() {
		return tag("ol");
	}

	public xwriter ol_() {
		return tage("ol");
	}

	public OutputStream outputstream() {
		return os;
	}

	public xwriter p(final a e) {
		return p(e.str());
	}

	public xwriter p(final boolean n) {
		return p(Boolean.toString(n));
	}

	public xwriter p(final byte n) {
		return p(Byte.toString(n));
	}

	public xwriter p(final char n) {
		return p(Character.toString(n));
	}

	public xwriter p(final CharSequence cs) {
		return p(cs.toString());
	}

	public xwriter p(final double n) {
		return p(Double.toString(n));
	}

	public xwriter p(final float n) {
		return p(Float.toString(n));
	}

	public xwriter p(final int n) {
		return p(Integer.toString(n));
	}

	public xwriter p(final long n) {
		return p(Long.toString(n));
	}

	public xwriter p(final String s) {
		if (s == null)
			return this;
		try {
			os.write(tobytes(s));
		} catch (final IOException e) {
			throw new Error(e);
		}
		return this;
	}

	public xwriter pl() {
		return nl();
	}

	public xwriter pl(final String s) {
		return p(s).nl();
	}

	public xwriter pre() {
		return pre(null);
	}

	public xwriter pre(final String cls) {
		tago("pre");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	public xwriter pre_() {
		return tage("pre");
	}

	/**
	 * Renders element.
	 * 
	 * @param e element
	 * @return this
	 * @throws Throwable
	 */
	public xwriter r(final a e) throws Throwable {
		return render(e);
	}

	/**
	 * Renders element.
	 * 
	 * @param e element
	 * @return this
	 * @throws Throwable
	 */
	public xwriter render(final a e) throws Throwable {
		if (e == null)
			return this;
		e.to(this);
		return this;
	}

	/**
	 * Opens 'script' tag.
	 * 
	 * @return this
	 */
	public xwriter script() {
		return tag("script");
	}

	/**
	 * Closes 'script' tag.
	 * 
	 * @return this
	 */
	public xwriter script_() {
		return tage("script");
	}

	/**
	 * Renders 'select' block.
	 *
	 * @param e       element.
	 * @param cls     style class or null/empty string if none
	 * @param style   style or null/empty string if none
	 * @param options text and value separated by |. Value is optional. If none
	 *                provided text is used.
	 * @return this
	 */
	public xwriter select(final a e, final String cls, final String style, final List<String> options) {
		selecto(e, cls, style).tagoe();
		select_options(e, options);
		return tage("select");
	}

	/**
	 * Renders select options list.
	 *
	 * @param e       element.
	 * @param options text and value separated by |. Value is optional. If none
	 *                provided text is used.
	 * @return this
	 */
	public xwriter select_options(final a e, final List<String> options) {
		final String v = tostr(e.str(), "");
		for (final String s : options) {
			final String[] sa = s.split("|");
			final String value = sa.length > 1 ? sa[1] : sa[0];
			tago("option").attr("value", value);
			if (value.equals(v)) {
				attr("selected");
			}
			tagoe().p(sa[0]);
		}
		return this;
	}

	/**
	 * Opens 'select' tag.
	 * 
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter selecto(final a e, final String cls, final String style) {
		return tago("select").default_attrs_for_element(e, cls, style);
	}

	/**
	 * Renders a complete 'span' with HTML escaped value of 'e'.
	 * 
	 * @return this
	 */
	public xwriter span(final a e) {
		return span(e, null, null);
	}

	/**
	 * Renders a complete 'span' with HTML escaped value of 'e'.
	 * 
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter span(final a e, final String cls) {
		return span(e, cls, null);
	}

	/**
	 * Renders a complete 'span' with HTML escaped value of 'e'.
	 * 
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter span(final a e, final String cls, final String style) {
		spano(e, cls, style).tagoe();
		try {
			e.to(new osltgt(os));
		} catch (final Throwable t) {
			throw new Error(t);
		}
		return span_();
	}

	/**
	 * Closes 'span' tag.
	 * 
	 * @return this
	 */
	public xwriter span_() {
		return tage("span");
	}

	/**
	 * Renders a complete 'span' with unescaped value of 'e'.
	 * 
	 * @param e     element
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter span_html(final a e, final String cls, final String style) {
		spano(e, cls, style).tagoe();
		try {
			e.to(this);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
		return span_();
	}

	/**
	 * Alias for 'span_html(...)'.
	 * 
	 * @param e element
	 * @return this
	 */
	public xwriter spanh(final a e) {
		return span_html(e, null, null);
	}

	/**
	 * Alias for 'span_html(...)'.
	 * 
	 * @param e   element
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter spanh(final a e, final String cls) {
		return span_html(e, cls, null);
	}

	/**
	 * Alias for 'span_html(...)'.
	 * 
	 * @param e     element
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter spanh(final a e, final String cls, final String style) {
		return span_html(e, cls, style);
	}

	/**
	 * Opens a 'span' tag with id of element, class and style allowing the appending
	 * of additional attributes. Must be closed with 'tagoe()'.
	 *
	 * @param e     element
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter spano(final a e, final String cls, final String style) {
		return tago("span").default_attrs_for_element(e, cls, style);
	}

	/**
	 * Outputs a space character.
	 * 
	 * @return this
	 */
	public xwriter spc() {
		return p(' ');
	}

	/**
	 * Opens 'style' tag.
	 * 
	 * @return this
	 */
	public xwriter style() {
		return p("<style>");
	}

	/**
	 * Closes 'style' tag.
	 * 
	 * @return this
	 */
	public xwriter style_() {
		return tage("style");
	}

	/**
	 * Outputs a tab character.
	 * 
	 * @return this
	 */
	public xwriter tab() {
		return p('\t');
	}

	/**
	 * Open 'table' tag.
	 * 
	 * @return this
	 */
	public xwriter table() {
		return table(null, null);
	}

	/**
	 * Open 'table' tag.
	 * 
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter table(final String cls) {
		return table(cls, null);
	}

	/**
	 * Open 'table' tag.
	 * 
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter table(final String cls, final String style) {
		tago("table");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		tagoe();
		return this;
	}

	/**
	 * Closes 'table' tag.
	 * 
	 * @return this
	 */
	public xwriter table_() {
		return tage("table");
	}

	/**
	 * Open tag. Example 'tag("a")' outputs "<a>".
	 * 
	 * @param name tag name
	 * @return this
	 */
	public xwriter tag(final String name) {
		return p("<").p(name).p(">");
	}

	/**
	 * End tag. Example 'tage("a")' outputs '</a>'.
	 * 
	 * @param name tag name
	 * @return this
	 */
	public xwriter tage(final String name) {
		return p("</").p(name).p(">");
	}

	/**
	 * Opens a tag. Example 'tago("a")' outputs '<a'.
	 * 
	 * @param name tag name
	 * @return this
	 */
	public xwriter tago(final String name) {
		return p("<").p(name);
	}

	/**
	 * Outputs '>'. Called after a tago(...) to close the tag.
	 * 
	 * @return this
	 */
	public xwriter tagoe() {
		return p(">");
	}

	/**
	 * Opens 'td' tag.
	 * 
	 * @return this
	 */
	public xwriter td() {
		return td(null, null);
	}

	/**
	 * Opens 'td' tag.
	 * 
	 * @param colspan column span
	 * @return this
	 */
	public xwriter td(final int colspan) {
		return td(colspan, null);
	}

	/**
	 * Opens 'td' tag.
	 * 
	 * @param colspan column span
	 * @param cls     style class or null/empty string if none
	 * @return this
	 */
	public xwriter td(final int colspan, final String cls) {
		tago("td").attr("colspan", colspan);
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	/**
	 * Opens 'td' tag.
	 * 
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter td(final String cls) {
		return td(cls, null);
	}

	/**
	 * Opens 'td' tag.
	 * 
	 * @param cls   style class or null/empty string if none
	 * @param style style or null/empty string if none
	 * @return this
	 */
	public xwriter td(final String cls, final String style) {
		tago("td");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		return tagoe();
	}

	/**
	 * Closes 'td' tag.
	 * 
	 * @return this
	 */
	public xwriter td_() {
		return tage("td");
	}

	/**
	 * Opens 'th' tag.
	 * 
	 * @return this
	 */
	public xwriter th() {
		return th(null);
	}

	/**
	 * Opens 'th' tag.
	 * 
	 * @param colspan column span
	 * @return this
	 */
	public xwriter th(final int colspan) {
		return tago("th").attr("colspan", colspan).tagoe();
	}

	/**
	 * Opens 'tr' tag.
	 * 
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter th(final String cls) {
		tago("th");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	public xwriter th_() {
		return tage("th");
	}

	/**
	 * Renders 'title' tag.
	 * 
	 * @param title
	 * @return this
	 */
	public xwriter title(final String title) {
		return tag("title").p(title).tage("title");
	}

	/**
	 * Opens 'tr'.
	 * 
	 * @return this
	 */
	public xwriter tr() {
		return tr(null);
	}

	/**
	 * Opens 'tr' tag.
	 * 
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter tr(final String cls) {
		tago("tr");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	/**
	 * Opens 'tr' tag.
	 * 
	 * @return this
	 */
	public xwriter tr_() {
		return tage("tr");
	}

	/**
	 * Opens 'ul' tag.
	 * 
	 * @return this
	 */
	public xwriter ul() {
		return ul(null);
	}

	/**
	 * Opens 'ul' tag.
	 * 
	 * @param cls style class or null/empty string if none
	 * @return this
	 */
	public xwriter ul(final String cls) {
		tago("ul");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	/**
	 * Closes 'ul' tag.
	 * 
	 * @return this
	 */
	public xwriter ul_() {
		return tage("ul");
	}

	/**
	 * Display alert.
	 * 
	 * @param s text
	 * @return this
	 */
	public xwriter xalert(final String s) {
		return p("ui.alert('").jsstr(s).pl("');");
	}

	/**
	 * Focus on element.
	 * 
	 * @param e element
	 * @return this
	 */
	public xwriter xfocus(final a e) {
		return p("$f('").p(e.id()).pl("');");
	}

	/**
	 * Focus on field.
	 * 
	 * @param id field id
	 * @return this
	 */
	public xwriter xfocus(final String id) {
		return p("$f('").p(id).pl("');");
	}

	/**
	 * Prints to element content.
	 * 
	 * @return this
	 */
	public xwriter xp(final a e, final String s) {
		return p("$p('").p(e.id()).p("','").jsstr(s).pl("');");
	}

	/**
	 * Requests page reload.
	 * 
	 * @return this
	 */
	public xwriter xreload() {
		xreload_requested = true;
		return this;
	}

	/**
	 * Scrolls page to top.
	 * 
	 * @return this
	 */
	public xwriter xscroll_to_top() {
		return pl("ui.scrollToTop();");
	}

	/**
	 * Sets page title.
	 * 
	 * @param s string
	 * @return this
	 */
	public xwriter xtitle(final String s) {
		return p("$t('").jsstr(s).pl("');");
	}

	/**
	 * Updates inner HTML of element.
	 * 
	 * @param e element
	 * @return this
	 */
	public xwriter xu(final a e) throws Throwable {
		return xu(e, false);
	}

	/**
	 * Updates inner HTML of element.
	 *
	 * @param e       element
	 * @param escltgt true to escape 'lt' and 'gt'
	 * @return this
	 */
	public xwriter xu(final a e, final boolean escltgt) throws Throwable {
		e.to(xub(e, true, escltgt));
		return xube();
	}

	/**
	 * Begin an update of inner or outer HTML of an element.
	 *
	 * @param e       element
	 * @param inner   true if update inner HTML otherwise update outer HTML
	 * @param escltgt true to escape 'lt' and 'gt'
	 * @return xwriter that will write a JavaScript string that updates the inner or
	 *         outer HTML. Must be closed with 'xube()'
	 */
	public xwriter xub(final a e, final boolean inner, final boolean escltgt) {
		is_xu_open = true;
		p("$").p(inner ? "s" : "o").p("('").p(e.id()).p("','");
		// return new xwriter(new osjsstr(escltgt?new osltgt(os):os));
		// note: commented line above generates byte code issue and exception
		// possible bug in java
		if (escltgt) {
			return new xwriter(new osjsstr(new osltgt(os)));
		}
		return new xwriter(new osjsstr(os));
	}

	/**
	 * Completes an xub(...) call by closing the JavaScript string.
	 * 
	 * @return this
	 */
	public xwriter xube() {
		is_xu_open = false;
		return pl("');");
	}

	/**
	 * Updates the value of rendered checkbox.
	 * 
	 * @param e element
	 * @return this
	 */
	public xwriter xucb(final a e) {
		p("$s('").p(e.id()).p("','").p(enc_js_str(e.str())).p("');");
		p("$('").p(e.id()).p("').checked=");
		if ("1".equals(e.str())) {
			p("true");
		} else {
			p("false");
		}
		return pl(";");
	}

	/**
	 * Updates element outer HTML.
	 * 
	 * @param e element
	 * @return this
	 */
	public xwriter xuo(final a e) throws Throwable {
		e.to(xub(e, false, false));
		return xube();
	}

	/**
	 * Updates value attribute of the HTML rendered by element. If HTML element does
	 * not have value attribute it replaces the inner HTML.
	 * 
	 * @param e element
	 * @return this
	 */
	public xwriter xuv(final a e) {
		return p("$s('").p(e.id()).p("','").p(enc_js_str(e.str())).pl("');");
	}

	@Override
	public String toString() {
		return os.toString();
	}
}
