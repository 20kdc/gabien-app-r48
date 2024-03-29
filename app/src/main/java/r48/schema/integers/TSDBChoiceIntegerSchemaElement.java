/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.integers;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.layouts.UIScrollLayout;
import gabien.uslx.append.*;
import r48.App;
import r48.dbs.TSDB;
import r48.ui.UIGrid;

/**
 * Choosing your fate, now with visuals.
 * Created on May 11th 2018.
 */
public class TSDBChoiceIntegerSchemaElement extends IntegerSchemaElement {
    public IImage tsdb;
    public int maxCount;
    public int tileSize;

    public TSDBChoiceIntegerSchemaElement(App app, long i, String substring, int pwr) {
        super(app, i);
        tileSize = 16;
        tsdb = new TSDB(app, substring).compileSheet(pwr, tileSize);
        maxCount = pwr;
    }

    @Override
    public ActiveInteger buildIntegerEditor(long oldVal, final IIntegerContext context) {
        final ActiveInteger ai = super.buildIntegerEditor(oldVal, context);
        final int sprScale = app.f.getSpriteScale();
        final UIGrid uig = new UIGrid(app, tileSize * sprScale, (tileSize * sprScale) + app.f.gridTH + 1, maxCount) {
            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
                super.drawTile(t, hover, x, y, igd);
                y += app.f.gridTH + 1;
                igd.blitScaledImage(t * tileSize, 0, tileSize, tileSize, x, y, tileSize * sprScale, tileSize * sprScale, tsdb);
            }

            @Override
            public Size layoutRecalculateMetricsImpl() {
                Size res = super.layoutRecalculateMetricsImpl();
                if (uivScrollbar != null) {
                    return new Size((16 * 16 * sprScale) + uivScrollbar.getWantedSize().width, res.height);
                } else {
                    return res;
                }
            }
        };
        uig.setSelected((int) oldVal);
        uig.onSelectionChange = () -> {
            context.update(uig.getSelected());
        };
        UIScrollLayout usl = context.newSVL();
        usl.panelsSet(uig, ai.uie);
        return new ActiveInteger(usl, (aLong) -> {
            Runnable osc = uig.onSelectionChange;
            uig.onSelectionChange = null;
            uig.setSelected((int) (long) aLong);
            uig.onSelectionChange = osc;
            ai.onValueChange.accept(aLong);
        });
    }
}
