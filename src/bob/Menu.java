// reviewed: 2024-08-05
package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

public final class Menu extends a {
    private final static long serialVersionUID = 1;

    private int selectedIndex;

    private final static class Item extends a {
        private final static long serialVersionUID = 1;

        Class<? extends a> cls;
        String title;

        public Item(final Class<? extends a> cls, final String title) {
            this.cls = cls;
            this.title = title;
        }

    }

    private final ArrayList<Item> items = new ArrayList<Item>();

    @Override
    public void to(final xwriter x) throws Throwable {
        final String id = id();
        // callback to 'x_s'
        x.selecto(this, null, null).attr("onchange", "$x('" + id + " s '+this.selectedIndex)").tagoe();
        int i = 0;
        for (final Item im : items) {
            x.tago("option").attr("value", i);
            if (i == selectedIndex) {
                x.attr("selected");
            }
            x.tagoe().p(im.title);
            i++;
        }
        x.tage("select");
    }

    public void addItem(final Class<? extends a> cls, final String title) {
        items.add(new Item(cls, title));
    }

    public void x_s(final xwriter x, final String param) throws Throwable {
        selectedIndex = Integer.parseInt(param);
        super.bubble_event(x, this, items.get(selectedIndex).cls);
    }

}
