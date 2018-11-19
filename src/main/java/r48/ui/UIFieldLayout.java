/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on November 19, 2018.
 */
public class UIFieldLayout extends UIElement.UIPanel {
    private final UIElement a, b;
    private final boolean hasOverride;
    private final AtomicInteger overrideValue;

    public UIFieldLayout(UIElement label, UIElement core, int fieldWidth, boolean fieldWidthOverride) {
        this(label, core, new AtomicInteger(fieldWidth), fieldWidthOverride);
    }

    public UIFieldLayout(UIElement label, UIElement core, AtomicInteger fieldWidth, boolean fieldWidthOverride) {
        a = label;
        b = core;
        layoutAddElement(a);
        layoutAddElement(b);
        hasOverride = fieldWidthOverride;
        overrideValue = fieldWidth;
    }

    @Override
    public void runLayout() {
        Size aWanted = a.getWantedSize(), bWanted = b.getWantedSize();
        Size mySize = getSize();
        int qWidth = aWanted.width;
        if (hasOverride)
            qWidth = overrideValue.get();
        int reqWidth = qWidth + bWanted.width;
        int reqHeight = Math.max(aWanted.height, bWanted.height);

        boolean performHorizontal = true;
        if (mySize.width < reqWidth) {
            qWidth = aWanted.width;
            int req2Width = qWidth + bWanted.width;
            if (mySize.width < req2Width) {
                // Cannot fit within constraints.
                performHorizontal = false;
                reqHeight = aWanted.height + bWanted.height;
            }
        }
        if (performHorizontal) {
            // use qWidth & fill rest with B
            a.setForcedBounds(this, new Rect(0, 0, qWidth, mySize.height));
            b.setForcedBounds(this, new Rect(qWidth, 0, mySize.width - qWidth, mySize.height));
        } else {
            int split = aWanted.height;
            a.setForcedBounds(this, new Rect(0, 0, mySize.width, split));
            b.setForcedBounds(this, new Rect(0, split, mySize.width, mySize.height - split));
        }
        setWantedSize(new Size(reqWidth, reqHeight));
    }
}
