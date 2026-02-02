/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.util.function.Function;

import gabien.ui.UIDynamicProxy;
import gabien.ui.UIElement;
import gabien.ui.elements.UIAdjuster;
import gabien.ui.layouts.UISplitterLayout;
import r48.R48;

/**
 * For testing
 * Created 10th May 2024.
 */
public class UITestFontSizes extends UIDynamicProxy {
    private final UIAdjuster adj;
    private UISplitterLayout lastSplitter;
    private int currentSize = 32;
    private final Function<Integer, UIElement> maker;
    public UITestFontSizes(R48 app, Function<Integer, UIElement> maker) {
        this.maker = maker;
        adj = new UIAdjuster(app.f.dialogWindowTH, 32, (value) -> {
            currentSize = (int) (long) value;
            if (currentSize < 1)
                currentSize = 1;
            refreshContents();
            return (long) currentSize;
        });
        refreshContents();
        forceToRecommended();
    }
    public void refreshContents() {
        if (lastSplitter != null)
            lastSplitter.release();
        lastSplitter = new UISplitterLayout(adj, maker.apply(currentSize), true, 0);
        dynProxySet(lastSplitter);
    }
}
