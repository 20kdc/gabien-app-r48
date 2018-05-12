/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.integers;

import gabien.IGrDriver;
import gabien.ui.IConsumer;
import gabien.ui.Size;
import gabien.ui.UIScrollLayout;
import r48.FontSizes;
import r48.dbs.TSDB;
import r48.ui.UIGrid;

/**
 * Choosing your fate, now with visuals.
 * Created on May 11th 2018.
 */
public class TSDBChoiceIntegerSchemaElement extends IntegerSchemaElement {
    public TSDB tsdb;
    public int maxCount;

    public TSDBChoiceIntegerSchemaElement(long i, String substring, int pwr) {
        super(i);
        tsdb = new TSDB(substring);
        maxCount = pwr;
    }

    @Override
    public ActiveInteger buildIntegerEditor(long oldVal, final IIntegerContext context) {
        UIScrollLayout usl = context.newSVL();
        final ActiveInteger ai = super.buildIntegerEditor(oldVal, context);
        final int sprScale = FontSizes.getSpriteScale();
        final UIGrid uig = new UIGrid(16 * sprScale, 24 * sprScale, maxCount) {
            @Override
            protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
                super.drawTile(t, hover, x, y, igd);
                tsdb.draw(x, y + (8 * sprScale), 0, (short) t, sprScale, igd);
            }

            @Override
            public void setWantedSize(Size size) {
                if (uivScrollbar != null) {
                    super.setWantedSize(new Size((16 * 16 * sprScale) + uivScrollbar.getWantedSize().width, size.height));
                } else {
                    super.setWantedSize(size);
                }
            }
        };
        uig.setSelected((int) oldVal);
        uig.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                context.update(uig.getSelected());
            }
        };
        usl.panelsAdd(uig);
        usl.panelsAdd(ai.uie);
        return new ActiveInteger(usl, new IConsumer<Long>() {
            @Override
            public void accept(Long aLong) {
                Runnable osc = uig.onSelectionChange;
                uig.onSelectionChange = null;
                uig.setSelected((int) (long) aLong);
                uig.onSelectionChange = osc;
                ai.onValueChange.accept(aLong);
            }
        });
    }
}
