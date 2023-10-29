/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import r48.App;
import r48.App.Pan;

/**
 * Split out from UIMTBase 13th March 2023 to get rid of those pesky "no real map context" uses of UIMTBase
 */
public class UIDynAppPrx extends Pan {

    private UIElement innerElem = null;
    public boolean selfClose = false;
    public boolean hasClosed = false;
    public String titleOverride;

    public UIDynAppPrx(@NonNull App app) {
        super(app);
    }

    protected void changeInner(UIElement inner, boolean inConstructor) {
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        if (innerElem != null)
            if (inConstructor)
                throw new RuntimeException("Stop it! >.<");
        innerElem = inner;
        if (inner != null) {
            if (inConstructor)
                inner.forceToRecommended();
            layoutAddElement(inner);
            // This is just to do the set forced bounds -> set wanted size thing.
            if (!inConstructor) {
                layoutRecalculateMetrics();
            } else {
                forceToRecommended();
            }
        }
    }

    @Override
    public boolean requestsUnparenting() {
        return selfClose;
    }

    @Override
    public int layoutGetHForW(int width) {
        if (innerElem != null)
            return innerElem.layoutGetHForW(width);
        return super.layoutGetHForW(width);
    }

    @Override
    public int layoutGetWForH(int height) {
        if (innerElem != null)
            return innerElem.layoutGetWForH(height);
        return super.layoutGetWForH(height);
    }

    @Override
    protected void layoutRunImpl() {
        if (innerElem != null)
            innerElem.setForcedBounds(this, new Rect(getSize()));
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        if (innerElem != null)
            return innerElem.getWantedSize();
        return null;
    }

    @Override
    public void onWindowClose() {
        hasClosed = true;
    }

    @Override
    public String toString() {
        if (titleOverride != null)
            return titleOverride;
        if (innerElem != null)
            return innerElem.toString();
        return super.toString();
    }

    public static UIDynAppPrx wrap(@NonNull App app, @NonNull UIElement svl) {
        UIDynAppPrx r = new UIDynAppPrx(app);
        r.changeInner(svl, true);
        return r;
    }
}