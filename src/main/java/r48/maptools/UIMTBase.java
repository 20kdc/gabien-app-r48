/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.map.IMapToolContext;
import r48.ui.IWindowElement;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Was used for many things that it shouldn't have been. Now, not so much,
 *  but frankly it's still too ridiculously convenient.
 * Maybe make a superclass for the convenient stuff?
 * Created on August 14 2017.
 */
public class UIMTBase extends UIElement.UIPanel implements IWindowElement {
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
            layoutAddElement(inner);
            if (inConstructor) {
                inner.forceToRecommended(this);
                setForcedBounds(null, new Rect(inner.getSize()));
            } else {
                runLayout();
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
            innerElem.setForcedBounds(this, new Rect(getSize()));
            setWantedSize(innerElem.getWantedSize());
        }
    }

    @Override
    public void windowClosing() {
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
            public void windowClosing() {
                super.windowClosing();
                cc.run();
            }
        };
        r.changeInner(svl, true);
        return r;
    }
}
