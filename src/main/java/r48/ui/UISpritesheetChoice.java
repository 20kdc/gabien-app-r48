/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Like UIEnumChoice in that this provides options to the user, but acts differently in that this provides a grid of images.
 * Created on 29/07/17.
 */
public class UISpritesheetChoice extends UIPanel {
    public UISplitterLayout rootLayout;
    public UIGrid spriteGrid;

    public UISpritesheetChoice(int oldVal, final ISpritesheetProvider provider, final IConsumer<Integer> consumer) {
        spriteGrid = new UIGrid(provider.itemWidth() * FontSizes.getSpriteScale(), provider.itemHeight() * FontSizes.getSpriteScale(), provider.itemCount()) {
            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrInDriver igd) {
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
        final UINumberBox nb = new UINumberBox(FontSizes.dialogWindowTextHeight);
        nb.number = oldVal;
        UISplitterLayout msp = new UISplitterLayout(nb, new UITextButton(FontSizes.dialogWindowTextHeight, TXDB.get("Sprite Num."), new Runnable() {
            @Override
            public void run() {
                consumer.accept(nb.number);
            }
        }), false, 1);
        rootLayout = new UISplitterLayout(spriteGrid, msp, true, 1);
        allElements.add(rootLayout);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        rootLayout.setBounds(new Rect(0, 0, r.width, r.height));
    }
}
