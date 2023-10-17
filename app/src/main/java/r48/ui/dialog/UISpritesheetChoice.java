/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import gabien.render.IGrDriver;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;
import r48.ui.UIGrid;

/**
 * Like UIEnumChoice in that this provides options to the user, but acts differently in that this provides a grid of images.
 * Created on 29/07/17.
 */
public class UISpritesheetChoice extends UIElement.UIProxy {
    public UISplitterLayout rootLayout;
    public UIGrid spriteGrid;

    public UISpritesheetChoice(App app, long oldVal, final ISpritesheetProvider provider, final Consumer<Long> consumer) {
        spriteGrid = new UIGrid(app, provider.itemWidth() * app.f.getSpriteScale(), provider.itemHeight() * app.f.getSpriteScale(), provider.itemCount()) {
            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
                provider.drawItem(provider.mapIdxToVal(t), x, y, app.f.getSpriteScale(), igd);
            }
        };
        spriteGrid.setSelected(provider.mapValToIdx(oldVal));
        spriteGrid.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                consumer.accept(provider.mapIdxToVal(spriteGrid.getSelected()));
            }
        };
        final UINumberBox nb = new UINumberBox(oldVal, app.f.dialogWindowTH);
        UISplitterLayout msp = new UISplitterLayout(nb, new UITextButton(app.t.u.spr_num, app.f.dialogWindowTH, new Runnable() {
            @Override
            public void run() {
                consumer.accept(nb.number);
            }
        }), false, 1);
        rootLayout = new UISplitterLayout(spriteGrid, msp, true, 1);
        proxySetElement(rootLayout, true);
    }
}
