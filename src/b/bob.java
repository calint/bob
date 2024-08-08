// reviewed: 2024-08-05
package b;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import db.Db;

/** Websocket that implements the framework. */
public class bob extends websock {
    private static final String axfld = "$";
    protected a controller;

    public bob() {
        super(true);
    }

    /**
     * Override to provide the root class.
     * 
     * @return root element class
     */
    protected Class<? extends a> root_class() {
        return a.class;
    }

    @Override
    protected final void on_opened() throws Throwable {
        try {
            Db.initCurrentTransaction();
            // todo load root from db or create new
            controller = (a) root_class().getConstructor().newInstance();

            final xwriter js = new xwriter();
            final xwriter x = js.xub(controller, true, false);
            controller.to(x);
            js.xube();

            send(js.toString());
        } finally {
            Db.deinitCurrentTransaction();
        }
    }

    @Override
    protected final void on_message(final ByteBuffer bb) throws Throwable {
        if (bb.remaining() == 0) {
            // a keep-alive message from bob.js
            return;
        }
        try {
            Db.initCurrentTransaction();

            final HashMap<String, String> content = populateContentMapFromBuffer(bb);
            String ajax_command_string = "";
            for (final Map.Entry<String, String> me : content.entrySet()) {
                if (axfld.equals(me.getKey())) {
                    ajax_command_string = me.getValue();
                    continue;
                }
                final String[] paths = me.getKey().split(a.id_path_separator);
                a e = controller;
                for (int n = 1; n < paths.length; n++) {
                    e = e.child(paths[n]);
                    if (e == null) {
                        throw new RuntimeException("not found: " + me.getKey());
                    }
                }
                e.set(me.getValue());
            }
            if (ajax_command_string.length() == 0) {
                throw new RuntimeException("expectedax");
            }

            // decode the field id, method name and parameters parameters
            final String target_elem_id, target_elem_method, target_elem_method_args;
            final int i1 = ajax_command_string.indexOf(' ');
            if (i1 == -1) {
                target_elem_id = ajax_command_string;
                target_elem_method = target_elem_method_args = "";
            } else {
                target_elem_id = ajax_command_string.substring(0, i1);
                final int i2 = ajax_command_string.indexOf(' ', i1 + 1);
                if (i2 == -1) {
                    target_elem_method = ajax_command_string.substring(i1 + 1);
                    target_elem_method_args = "";
                } else {
                    target_elem_method = ajax_command_string.substring(i1 + 1, i2);
                    target_elem_method_args = ajax_command_string.substring(i2 + 1);
                }
            }
            // navigate to the target element
            final String[] path = target_elem_id.split(a.id_path_separator);
            a target_elem = controller;
            for (int n = 1; n < path.length; n++) {
                target_elem = target_elem.child(path[n]);
                if (target_elem == null) {
                    break;
                }
            }
            // invoke method on target element with arguments
            final xwriter x = new xwriter();
            if (target_elem == null) {
                x.xalert("element not found:\n" + target_elem_id);
                x.finish();
                final String msg = x.toString();
                send(msg);
                return;
            }
            try {
                target_elem.getClass().getMethod("x_" + target_elem_method, xwriter.class, String.class)
                        .invoke(target_elem, x, target_elem_method_args);
            } catch (final InvocationTargetException t) {
                b.log(t.getTargetException());
                Throwable e = t;
                while (e.getCause() != null) {
                    e = e.getCause();
                }
                x.close_update_if_open();
                x.xalert(e.toString());
            } catch (final NoSuchMethodException t) {
                x.close_update_if_open();
                x.xalert("method not found:\npublic x_" + target_elem_method + "(xwriter,String) in "
                        + target_elem.getClass().getName() + " or it's super classes.");
            }
            x.finish();
            final String msg = x.toString();
            send(msg);
        } finally {
            Db.deinitCurrentTransaction();
        }
    }

    @Override
    protected final void on_closed() throws Throwable {
        // todo store root in db
    }

    private static HashMap<String, String> populateContentMapFromBuffer(final ByteBuffer bb) throws Throwable {
        final HashMap<String, String> content = new HashMap<String, String>();
        final byte[] ba = bb.array();
        int start = bb.position();
        int end = start;
        int i = start;
        final int lim = bb.limit();
        String name = "";
        int state = 0;
        while (start != lim) {
            final byte c = ba[i];
            // todo: can be done without a state switch at every character
            switch (state) {
            case 0:
                if (c == '=') {
                    name = new String(ba, start, end - start, b.strenc);
                    start = end + 1;
                    state = 1;
                }
                break;
            case 1:
                if (c == '\r') {
                    final String value = new String(ba, start, end - start, b.strenc);
                    content.put(name, value);
                    start = end + 1;
                    state = 0;
                }
                break;
            default:
                throw new RuntimeException();
            }
            i++;
            end++;
        }
        return content;
    }
}
