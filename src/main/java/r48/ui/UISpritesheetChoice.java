/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
        spriteGrid = new UIGrid(provider.itemWidth(), provider.itemHeight(), provider.itemCount()) {
            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrInDriver igd) {
                provider.drawItem(provider.mapIdxToVal(t), x, y, igd);
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
