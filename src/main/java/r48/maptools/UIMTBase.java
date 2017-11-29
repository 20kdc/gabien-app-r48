/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.ui.IWindowElement;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.map.IMapToolContext;

/**
 * Created on August 14 2017.
 * This is used from several places, not necessarily map tools,
 *  because it's a good base class for anonymous classes (among other things)
 */
public class UIMTBase extends UIPanel implements IWindowElement {
    private UIElement innerElem = null;
    private boolean forceSz;

    public final IMapToolContext mapToolContext;

    public boolean selfClose = false;
    public boolean hasClosed = false;

    public UIMTBase(IMapToolContext mtc, final boolean forceSize) {
        forceSz = forceSize;
        mapToolContext = mtc;
    }

    protected void changeInner(UIElement inner) {
        allElements.clear();
        innerElem = inner;
        if (inner != null) {
            allElements.add(inner);
            setBounds(inner.getBounds());
        }
    }

    @Override
    public void setBounds(Rect r) {
        if (innerElem != null) {
            if (forceSz) {
                Rect s = innerElem.getBounds();
                innerElem.setBounds(new Rect(0, 0, s.width, s.height));
                super.setBounds(new Rect(r.x, r.y, s.width, s.height));
                return;
            }
            innerElem.setBounds(new Rect(0, 0, r.width, r.height));
        }
        super.setBounds(r);
    }

    @Override
    public boolean wantsSelfClose() {
        return selfClose;
    }

    @Override
    public void windowClosed() {
        hasClosed = true;
    }

    @Override
    public String toString() {
        if (innerElem != null)
            return innerElem.toString();
        return super.toString();
    }

    public static UIMTBase wrap(IMapToolContext mtc, UIElement svl, boolean forceSize) {
        UIMTBase r = new UIMTBase(mtc, forceSize);
        r.changeInner(svl);
        return r;
    }
}
