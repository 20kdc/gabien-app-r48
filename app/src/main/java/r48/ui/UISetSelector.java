/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import gabien.ui.UIElement;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Set selector given toStringable targets
 * Created on 13th August 2022.
 */
public class UISetSelector<T> extends UIProxy {
    private HashSet<T> set = new HashSet<T>();
    private HashMap<T, UITextButton> setButtons = new HashMap<T, UITextButton>();
    private UIScrollLayout layout = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public UISetSelector(final Iterable<T> entries) {
        layout.panelsAdd(new UITextButton(TXDB.get("All Selected"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                for (T x : entries)
                    set.add(x);
                refreshContents();
            }
        }));
        layout.panelsAdd(new UITextButton(TXDB.get("All Deselected"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                set.clear();
                refreshContents();
            }
        }));
        for (T o : entries) {
            final T fo = o;
            final UITextButton utb = new UITextButton(o.toString(), FontSizes.dialogWindowTextHeight, null);
            utb.onClick = new Runnable() {
                @Override
                public void run() {
                    if (utb.state) {
                        set.add(fo);
                    } else {
                        set.remove(fo);
                    }
                    refreshContents();
                }
            };
            layout.panelsAdd(utb);
            setButtons.put(o, utb);
        }
        proxySetElement(layout, true);
    }
    private void refreshContents() {
        for (UITextButton utb : setButtons.values())
            utb.state = false;
        for (Object o : set)
            setButtons.get(o).state = true;
    }
    public Set<T> getSet() {
        return new HashSet<T>(set);
    }
    public void updateSet(Set<T> newSet) {
        set.clear();
        set.addAll(newSet);
        refreshContents();
    }
}
