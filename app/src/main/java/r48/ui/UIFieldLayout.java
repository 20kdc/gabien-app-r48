/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;

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
    protected void layoutRunImpl() {
        Size aWanted = a.getWantedSize(), bWanted = b.getWantedSize();
        Size mySize = getSize();
        int qWidth = aWanted.width;
        if (hasOverride)
            qWidth = overrideValue.get();
        int reqWidth = qWidth + bWanted.width;

        boolean performHorizontal = true;
        if (mySize.width < reqWidth) {
            qWidth = aWanted.width;
            int req2Width = qWidth + bWanted.width;
            // Cannot fit within constraints.
            if (mySize.width < req2Width)
                performHorizontal = false;
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
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        Size aWanted = a.getWantedSize(), bWanted = b.getWantedSize();
        int qWidth = aWanted.width;
        if (hasOverride)
            qWidth = overrideValue.get();
        int reqWidth = qWidth + bWanted.width;
        int reqHeight = Math.max(aWanted.height, bWanted.height);
        return new Size(reqWidth, reqHeight);
    }

    @Override
    public int layoutGetHForW(int width) {
        Size aWanted = a.getWantedSize(), bWanted = b.getWantedSize();
        int qWidth = aWanted.width;
        if (hasOverride)
            qWidth = overrideValue.get();
        int reqWidth = qWidth + bWanted.width;
        int reqHeight = Math.max(aWanted.height, bWanted.height);

        if (width < reqWidth) {
            qWidth = aWanted.width;
            int req2Width = qWidth + bWanted.width;
            // Cannot fit within constraints.
            if (width < req2Width)
                reqHeight = aWanted.height + bWanted.height;
        }
        return reqHeight;
    }
}
