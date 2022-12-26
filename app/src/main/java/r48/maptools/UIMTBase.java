/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.map.IMapToolContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Was used for many things that it shouldn't have been. Now, not so much,
 *  but frankly it's still too ridiculously convenient.
 * Maybe make a superclass for the convenient stuff?
 * Created on August 14 2017.
 */
public class UIMTBase extends UIElement.UIPanel {
    private UIElement innerElem = null;

    public final IMapToolContext mapToolContext;

    public boolean selfClose = false;
    public boolean hasClosed = false;
    public String titleOverride;

    public UIMTBase(IMapToolContext mtc) {
        mapToolContext = mtc;
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

    public static UIMTBase wrap(IMapToolContext mtc, UIElement svl) {
        UIMTBase r = new UIMTBase(mtc);
        r.changeInner(svl, true);
        return r;
    }

    public static UIMTBase wrapWithCloseCallback(IMapToolContext mtc, final UIElement svl, final AtomicBoolean baseCloser, final Runnable cc) {
        UIMTBase r = new UIMTBase(mtc) {
            @Override
            public boolean requestsUnparenting() {
                if (baseCloser != null)
                    return baseCloser.get();
                return svl.requestsUnparenting();
            }

            @Override
            public void onWindowClose() {
                // Intentional super, as this is a subclass of UIMTBase.
                super.onWindowClose();
                cc.run();
            }
        };
        r.changeInner(svl, true);
        return r;
    }
}
