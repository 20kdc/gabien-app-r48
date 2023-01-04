/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gabien.ui.UIElement.UIProxy;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.ui.spacing.UIIndentThingy;

/**
 * Set selector given toStringable targets
 * Created on 13th August 2022.
 */
public class UISetSelector<T> extends UIProxy {
    private HashSet<T> set = new HashSet<T>();
    private HashMap<T, UIIndentThingy> setButtons = new HashMap<T, UIIndentThingy>();
    private HashMap<T, UILabel> setLabels = new HashMap<T, UILabel>();
    private UIScrollLayout layout = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public UISetSelector(final Iterable<T> entries) {
        layout.panelsAdd(new UITextButton(TXDB.get("Select All"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                for (T x : entries)
                    set.add(x);
                refreshContents();
            }
        }));
        layout.panelsAdd(new UITextButton(TXDB.get("Deselect All"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                set.clear();
                refreshContents();
            }
        }));
        int labelHeight = UILabel.getRecommendedTextSize("", FontSizes.dialogWindowTextHeight).height;
        for (T o : entries) {
            final T fo = o;
            final UILabel ul = new UILabel(o.toString(), FontSizes.dialogWindowTextHeight);
            final UIIndentThingy utb = new UIIndentThingy(0, labelHeight, 0, 0, new Runnable() {
                @Override
                public void run() {
                    if (set.contains(fo)) {
                        set.remove(fo);
                    } else {
                        set.add(fo);
                    }
                    refreshContents();
                }
            });
            layout.panelsAdd(new UISplitterLayout(utb, ul, false, 0));
            setButtons.put(o, utb);
            setLabels.put(o, ul);
        }
        proxySetElement(layout, true);
        refreshContents();
    }
    private void refreshContents() {
        for (UIIndentThingy utb : setButtons.values())
            utb.selected = UIIndentThingy.SELECTED_NOT_THIS;
        for (Object o : set)
            setButtons.get(o).selected = UIIndentThingy.SELECTED_HEAD;
    }
    public Set<T> getSet() {
        return new HashSet<T>(set);
    }
    public void updateSet(Set<T> newSet) {
        set.clear();
        set.addAll(newSet);
        refreshContents();
    }
    public void refreshButtonText() {
        // System.out.println("Refreshing button text!!!");
        for (Map.Entry<T, UILabel> buttons : setLabels.entrySet())
            buttons.getValue().text = buttons.getKey().toString();
    }
}
