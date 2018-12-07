/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyCT;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Colours, tones, same thing
 * Created on 1/3/17.
 */
public class CTNativeSchemaElement extends SchemaElement {
    public final String cls;

    public CTNativeSchemaElement(String c) {
        cls = c;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, this, target);
        RubyCT rct = new RubyCT(target.getBuffer());
        addField(uiSVL, TXDB.get("R"), 0, rct, path);
        addField(uiSVL, TXDB.get("G"), 8, rct, path);
        addField(uiSVL, TXDB.get("B"), 16, rct, path);
        addField(uiSVL, TXDB.get("A/L"), 24, rct, path);
        return uiSVL;
    }

    private void addField(UIScrollLayout uiSVL, String r, final int i, final RubyCT targ, final SchemaPath sp) {
        final UINumberBox uinb = new UINumberBox((long) targ.innerTable.getDouble(i), FontSizes.schemaFieldTextHeight);
        uinb.onEdit = new Runnable() {
            @Override
            public void run() {
                if (cls.equals("Tone")) {
                    if (uinb.number < -255)
                        uinb.number = -255;
                } else {
                    if (uinb.number < 0)
                        uinb.number = 0;
                }
                if (uinb.number > 255)
                    uinb.number = 255;
                targ.innerTable.putDouble(i, uinb.number);
                sp.changeOccurred(false);
            }
        };
        uiSVL.panelsAdd(new UISplitterLayout(new UILabel(r, FontSizes.schemaFieldTextHeight), uinb, false, 1, 3));
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (checkType(target, 'u', cls, setDefault)) {
            byte[] buf = new byte[32];
            target.setUser(cls, buf);
            RubyCT rct = new RubyCT(buf);
            rct.innerTable.putDouble(0, 0);
            rct.innerTable.putDouble(8, 0);
            rct.innerTable.putDouble(16, 0);
            rct.innerTable.putDouble(24, 255);
            path.changeOccurred(true);
        }
    }
}
