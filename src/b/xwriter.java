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
    /**
     * Encodes double quotes to HTML encoding and single quote JavaScript string.
     * 
     * @param str string
     * @return encoded string
     */
    public static String enc_js_in_attr(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace("'", "\\'").replace("\"", "&quot;");
    }

    /**
     * Encodes string for single quote JavaScript string.
     * 
     * @param str string
     * @return encoded string
     */
    public static String enc_js_str(final String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "\\'");
    }

    /**
     * Encodes string for double quote HTML string.
     * 
     * @param str string
     * @return encoded string
     */
    public static String enc_quot(final String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\"", "&quot;");
    }

    private boolean is_xu_open;
    // note: if xu is open then it needs to be closed at error messaging to client
    private final OutputStream os;
    private boolean xreload_requested; // reload page races with element serialization to db

    public xwriter() {
        os = new ByteArrayOutputStream();
    }

    public xwriter(final OutputStream os) {
        this.os = os;
    }

    /**
     * Returns the output stream wrapped by this.
     * 
     * @return this
     */
    public OutputStream outputstream() {
        return os;
    }

    /**
     * Renders '<a href ...>' open tag.
     * 
     * @param href
     * @return this
     */
    public xwriter a(final String href) {
        return tago("a").attr("href", enc_quot(href)).tagoe();
    }

    /**
     * Renders '<a href ...>html</a>' block.
     * 
     * @param href
     * @param html
     * @return this
     */
    public xwriter a(final String href, final String html) {
        return a(href).p(html).a_();
    }

    /**
     * Closes 'a' tag.
     * 
     * @return this
     */
    public xwriter a_() {
        return tage("a");
    }

    /**
     * Renders HTML tag attribute without value.
     * 
     * @param name
     * @return this
     */
    public xwriter attr(final String name) {
        return p(" ").p(name);
    }

    /**
     * Renders HTML tag attribute with value.
     * 
     * @param name  attribute name
     * @param value attribute value
     * @return this
     */
    public xwriter attr(final String name, final int value) {
        return p(" ").p(name).p("=").p(value);
    }

    /**
     * Renders HTML tag attribute with double quotes escaped value.
     * 
     * @param name  attribute name
     * @param value attribute value
     * @return this
     */
    public xwriter attr(final String name, final String value) {
        return p(" ").p(name).p("=\"").p(enc_quot(value)).p("\"");
    }

    /**
     * Generates a href tag with JavaScript code for a callback.
     * 
     * @param e        element
     * @param callback callback function name without prefix 'x_' and the rest is
     *                 parameter
     * @param html     HTML in href tag
     * @return this
     */
    public xwriter ax(final a e, final String callback, final String html) {
        return ax(e, callback, html, null);
    }

    /**
     * Generates a href tag with JavaScript code for a callback.
     * 
     * @param e        element
     * @param callback callback function name without prefix 'x_' and the rest is
     *                 parameter
     * @param html     HTML in href tag
     * @param cls      style class
     * @return this
     */
    public xwriter ax(final a e, final String callback, final String html, final String cls) {
        p("<a ");
        if (cls != null) {
            p(" class=\"").p(cls).p("\" ");
        }
        p("href=\"javascript:");
        js_x(e, callback, true);
        p("\">").p(html).p("</a>");
        return this;
    }

    /**
     * Renders 'br' tag.
     * 
     * @return this
     */
    public xwriter br() {
        return tag("br");
    }

    /**
     * Opens 'code' tag.
     * 
     * @return this
     */
    public xwriter code() {
        return tag("code");
    }

    /**
     * Closes 'code' tag.
     * 
     * @return this
     */
    public xwriter code_() {
        return tage("code");
    }

    /**
     * Renders CSS style for an element.
     * 
     * @param e     element
     * @param style CSS style
     * @return this
     */
    public xwriter css(final a e, final String style) {
        return p("#").p(e.id()).p("{").p(style).p("}");
    }

    /**
     * Renders CSS style for an element.
     * 
     * @param e        element
     * @param selector CSS selector
     * @param style    CSS style
     * @return this
     */
    public xwriter css(final a e, final String selector, final String style) {
        return css("#" + e.id() + " " + selector, style);
    }

    /**
     * Renders CSS style for selector.
     * 
     * @param selector CSS selector
     * @param style    CSS style
     * @return this
     */
    public xwriter css(final String selector, final String style) {
        return p(selector).p("{").p(style).p("}");
    }

    /**
     * Renders CSS element that loads a font.
     * 
     * @param name font name
     * @param url  URL to font
     * @return this
     */
    public xwriter cssfont(final String name, final String url) {
        return p("@font-face{font-family:").p(name).p(";src:url(").p(url).p(");}");
    }

    /**
     * Renders default attributes for HTML element.
     * 
     * @param e element
     * @return this
     */
    public xwriter default_attrs_for_element(final a e) {
        return default_attrs_for_element(e, null, null);
    }

    /**
     * Renders default attributes for HTML element.
     * 
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
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

    /**
     * Renders a div with HTML escaped element output.
     * 
     * @param e element
     * @return this
     */
    public xwriter div(final a e) {
        return div(e, null, null);
    }

    /**
     * Renders a div with HTML escaped element output.
     * 
     * @param e   element
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter div(final a e, final String cls) {
        return div(e, cls, null);
    }

    /**
     * Renders a div with HTML escaped element output of `to(OutputStream)`.
     * 
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
    public xwriter div(final a e, final String cls, final String style) {
        divo(e, cls, style).tagoe();
        try {
            e.to(new osltgt(os));
        } catch (final Throwable t) {
            throw new Error(t);
        }
        return div_();
    }

    /**
     * Closes a 'div' tag.
     * 
     * @return this
     */
    public xwriter div_() {
        return tage("div");
    }

    /**
     * Renders a 'div' with HTML output of the element.
     * 
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
    public xwriter div_html(final a e, final String cls, final String style) {
        return divh(e, cls, style);
    }

    /**
     * Renders a 'div' with HTML output of the element.
     * 
     * @param e element
     * @return this
     */
    public xwriter divh(final a e) {
        return divh(e, null, null);
    }

    /**
     * Renders a 'div' with HTML output of by the element.
     * 
     * @param e   element
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter divh(final a e, final String cls) {
        return divh(e, cls, null);
    }

    /**
     * Renders a 'div' with HTML output of element `to(xwriter)`.
     * 
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
    public xwriter divh(final a e, final String cls, final String style) {
        divo(e, cls, style).tagoe();
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
     * 
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
    public xwriter divo(final a e, final String cls, final String style) {
        tago("div").default_attrs_for_element(e, cls, style);
        return this;
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

    /**
     * Closes 'xub(...)' is open.
     * 
     * @return this
     */
    public xwriter close_update_if_open() {
        if (!is_xu_open)
            return this;
        is_xu_open = false;
        return xube();
    }

    /**
     * Flushes output stream.
     * 
     * @return this
     */
    public xwriter flush() {
        try {
            os.flush();
            return this;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders a 'script' block that focuses on element. Intended to be used from
     * HTML renderer.
     * 
     * @param e element
     * @return this
     */
    public xwriter focus(final a e) {
        return script().p("$f('").p(e.id()).p("')").script_();
    }

    /**
     * Render 'hr' tag.
     * 
     * @return this
     */
    public xwriter hr() {
        return tag("hr");
    }

    /**
     * Input field.
     * 
     * @param e                          element
     * @param type                       HTML type
     * @param cls                        style class or null/empty string if none
     * @param style                      style or null/empty string if none
     * @param default_value              default value or null
     * @param on_enter_callback_elem     element to callback when key 'enter' is
     *                                   pressed
     * @param on_enter_callback          callback function name without prefix 'x_'
     *                                   and the rest is parameter
     * @param on_change_callback_elem    element to callback when field changes
     * @param on_change_callbackcallback function name without prefix 'x_' and the
     *                                   rest is parameter
     * @return this
     */
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

    /**
     * Input float.
     * 
     * @param e element
     * @return this
     */
    public xwriter inpflt(final a e) {
        return tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text")
                .attr("class", "nbr").attr("size", 5).attr("oninput", "$b(this)").tagoe();
    }

    /**
     * Input int.
     * 
     * @param e element
     * @return this
     */
    public xwriter inpint(final a e) {
        return tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text")
                .attr("class", "nbr").attr("size", 5).attr("oninput", "$b(this)").tagoe();
    }

    /**
     * Input long.
     * 
     * @param e element
     * @return this
     */
    public xwriter inplng(final a e) {
        return inpint(e);
    }

    /**
     * Input text field.
     * 
     * @param e element
     * @return this
     */
    public xwriter inptxt(final a e) {
        return inp(e, null, null, null, null, null, null, null, null);
    }

    /**
     * Input text field.
     * 
     * @param e                      element
     * @param callback_elem_on_enter element to callback when key 'enter' is entered
     * @return this
     */
    public xwriter inptxt(final a e, final a callback_elem_on_enter) {
        return inp(e, null, null, null, null, callback_elem_on_enter, null, null, null);
    }

    /**
     * Input text field.
     * 
     * @param e                      element
     * @param callback_elem_on_enter element to callback when key 'enter' is entered
     * @param callback               the callback where first word is method name
     *                               without prefix 'x_' and the rest is the
     *                               parameter
     * @return this
     */
    public xwriter inptxt(final a e, final a callback_elem_on_enter, final String callback) {
        return inp(e, null, null, null, null, callback_elem_on_enter, callback, null, null);
    }

    /**
     * Input text field.
     * 
     * @param e   element
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter inptxt(final a e, final String cls) {
        return inp(e, null, cls, null, null, null, null, null, null);
    }

    /**
     * Input text field.
     * 
     * @param e                      element
     * @param cls                    style class or null/empty string if none
     * @param callback_elem_on_enter element to callback when key 'enter' is entered
     * @param callback               the callback where first word is method name
     *                               without prefix 'x_' and the rest is the
     *                               parameter
     * @return this
     */
    public xwriter inptxt(final a e, final String cls, final a callback_elem_on_enter, final String callback) {
        return inp(e, null, cls, null, null, callback_elem_on_enter, callback, null, null);
    }

    /**
     * Input text field.
     * 
     * @param e                      element
     * @param cls                    style class or null/empty string if none
     * @param default_value          default value or null
     * @param callback_elem_on_enter element to callback when key 'enter' is entered
     * @param callback               the callback where first word is method name
     *                               without prefix 'x_' and the rest is the
     *                               parameter
     * @return this
     */
    public xwriter inptxt(final a e, final String cls, final String default_value, final a callback_elem_on_enter,
            final String callback) {
        return inp(e, null, cls, null, default_value, callback_elem_on_enter, callback, null, null);
    }

    /**
     * Input text area.
     * 
     * @param e element
     * @return this
     */
    public xwriter inptxtarea(final a e) {
        return inptxtarea(e, null, null);
    }

    /**
     * Input text area.
     * 
     * @param e   element
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter inptxtarea(final a e, final String cls) {
        return inptxtarea(e, cls, null);
    }

    /**
     * Input text area.
     * 
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
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
     * @param callback             first word is method name without 'x_' prefix and
     *                             remaining string is the parameter
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

    /**
     * Writes a string encoded for use in single quote delimited JavaScript.
     * 
     * @param s string
     * @return this
     */
    @SuppressWarnings("resource") // osjsstr does not need to do any cleanup
    public xwriter jsstr(final String s) {
        try {
            new osjsstr(os).write(tobytes(s));
            return this;
        } catch (final IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Opens 'li' tag.
     * 
     * @return this
     */
    public xwriter li() {
        return li(null);
    }

    /**
     * Opens 'li' tag.
     * 
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter li(final String cls) {
        if (isempty(cls))
            return li();
        return tago("li").attr("class", cls).tagoe();
    }

    /**
     * Opens 'ol' tag.
     * 
     * @return this
     */
    public xwriter ol() {
        return tag("ol");
    }

    /**
     * Closes 'ol' tag.
     * 
     * @return this
     */
    public xwriter ol_() {
        return tage("ol");
    }

    /**
     * Prints HTML &nbsp;.
     * 
     * @return this
     */
    public xwriter nbsp() {
        return p("&nbsp;");
    }

    /**
     * Prints a newline character.
     * 
     * @return this
     */
    public xwriter nl() {
        return p('\n');
    }

    /**
     * Prints a carriage return.
     * 
     * @return this
     */
    public xwriter enter() {
        return p('\r');
    }

    /**
     * Prints bell character.
     * 
     * @return this
     */
    public xwriter bell() {
        return p('\07');
    }

    /**
     * Prints value of element 'e'. Note: it does not print the rendered output of
     * the element.
     * 
     * @param e element
     * @return this
     */
    public xwriter p(final a e) {
        return p(e.str());
    }

    /**
     * Prints boolean.
     * 
     * @param bool boolean
     * @return this
     */
    public xwriter p(final boolean bool) {
        return p(Boolean.toString(bool));
    }

    /**
     * Prints byte.
     * 
     * @param n number
     * @return this
     */
    public xwriter p(final byte n) {
        return p(Byte.toString(n));
    }

    /**
     * Prints character.
     * 
     * @param ch character
     * @return this
     */
    public xwriter p(final char ch) {
        return p(Character.toString(ch));
    }

    /**
     * Prints character sequence.
     * 
     * @param cs character sequence
     * @return this
     */
    public xwriter p(final CharSequence cs) {
        return p(cs.toString());
    }

    /**
     * Prints double.
     * 
     * @param n number
     * @return this
     */
    public xwriter p(final double n) {
        return p(Double.toString(n));
    }

    /**
     * Prints float.
     * 
     * @param n number
     * @return this
     */
    public xwriter p(final float n) {
        return p(Float.toString(n));
    }

    /**
     * Prints int.
     * 
     * @param n number
     * @return this
     */
    public xwriter p(final int n) {
        return p(Integer.toString(n));
    }

    /**
     * Prints long.
     * 
     * @param n number
     * @return this
     */
    public xwriter p(final long n) {
        return p(Long.toString(n));
    }

    /**
     * Prints string.
     * 
     * @param s string
     * @return this
     */
    public xwriter p(final String s) {
        if (s == null) {
            return this;
        }
        try {
            os.write(tobytes(s));
        } catch (final IOException e) {
            throw new Error(e);
        }
        return this;
    }

    /**
     * Prints newline.
     * 
     * @return this
     */
    public xwriter pl() {
        return nl();
    }

    /**
     * Prints string followed by newline.
     * 
     * @param s text
     * @return this
     */
    public xwriter pl(final String s) {
        return p(s).nl();
    }

    /**
     * Opens tag 'pre'.
     * 
     * @return this
     */
    public xwriter pre() {
        return pre(null);
    }

    /**
     * Opens tag 'pre'.
     * 
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter pre(final String cls) {
        tago("pre");
        if (!isempty(cls)) {
            attr("class", cls);
        }
        return tagoe();
    }

    /**
     * Closes tag 'pre'.
     * 
     * @return this
     */
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
     * @param e       element
     * @param cls     style class or null/empty string if none
     * @param style   style or null/empty string if none
     * @param options text and optional value separated by |
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
     * @param options text and optional value separated by |
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
     * @param e     element
     * @param cls   style class or null/empty string if none
     * @param style style or null/empty string if none
     * @return this
     */
    public xwriter selecto(final a e, final String cls, final String style) {
        return tago("select").default_attrs_for_element(e, cls, style);
    }

    /**
     * Renders a complete 'span' with HTML escaped output of element.
     * 
     * @param e element
     * @return this
     */
    public xwriter span(final a e) {
        return span(e, null, null);
    }

    /**
     * Renders a complete 'span' with HTML escaped output of element.
     * 
     * @param e   element
     * @param cls style class or null/empty string if none
     * @return this
     */
    public xwriter span(final a e, final String cls) {
        return span(e, cls, null);
    }

    /**
     * Renders a complete 'span' with HTML escaped output of element.
     * 
     * @param e     element
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
