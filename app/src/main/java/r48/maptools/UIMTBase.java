/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.UIElement;
import r48.map.IMapToolContext;
import r48.ui.UIDynAppPrx;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Was used for many things that it shouldn't have been. Now, not so much,
 *  but frankly it's still too ridiculously convenient.
 * Maybe make a superclass for the convenient stuff?
 * Created on August 14 2017.
 */
public class UIMTBase extends UIDynAppPrx {
    public final IMapToolContext mapToolContext;

    public UIMTBase(@NonNull IMapToolContext mtc) {
        super(mtc.getMapView().app);
        mapToolContext = mtc;
    }

    public static UIMTBase wrapUIMT(@NonNull IMapToolContext mtc, @NonNull UIElement svl) {
        UIMTBase r = new UIMTBase(mtc);
        r.changeInner(svl, true);
        return r;
    }
}
