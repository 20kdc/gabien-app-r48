/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.dialog;

import gabien.IGrDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.ui.UIGrid;

/**
 * Like UIEnumChoice in that this provides options to the user, but acts differently in that this provides a grid of images.
 * Created on 29/07/17.
 */
public class UISpritesheetChoice extends UIElement.UIProxy {
    public UISplitterLayout rootLayout;
    public UIGrid spriteGrid;

    public UISpritesheetChoice(long oldVal, final ISpritesheetProvider provider, final IConsumer<Long> consumer) {
        spriteGrid = new UIGrid(provider.itemWidth() * FontSizes.getSpriteScale(), provider.itemHeight() * FontSizes.getSpriteScale(), provider.itemCount()) {
            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
                provider.drawItem(provider.mapIdxToVal(t), x, y, FontSizes.getSpriteScale(), igd);
            }
        };
        spriteGrid.setSelected(provider.mapValToIdx(oldVal));
        spriteGrid.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                consumer.accept(provider.mapIdxToVal(spriteGrid.getSelected()));
            }
        };
        final UINumberBox nb = new UINumberBox(oldVal, FontSizes.dialogWindowTextHeight);
        UISplitterLayout msp = new UISplitterLayout(nb, new UITextButton(TXDB.get("Sprite Num."), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                consumer.accept(nb.number);
            }
        }), false, 1);
        rootLayout = new UISplitterLayout(spriteGrid, msp, true, 1);
        proxySetElement(rootLayout, true);
    }
}
