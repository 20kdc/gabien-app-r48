/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
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
                runLayout();
            } else {
                setForcedBounds(null, new Rect(inner.getSize()));
            }
        }
    }

    @Override
    public boolean requestsUnparenting() {
        return selfClose;
    }

    @Override
    public void runLayout() {
        if (innerElem != null) {
            // If it doesn't change anything, this won't work very well
            boolean cannotSFB = innerElem.getSize().sizeEquals(getSize());
            if (!cannotSFB) {
                innerElem.setForcedBounds(this, new Rect(getSize()));
            } else {
                innerElem.runLayoutLoop();
            }
            setWantedSize(innerElem.getWantedSize());
        }
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