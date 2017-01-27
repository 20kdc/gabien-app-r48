/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UINumberBox;
import r48.RubyCT;
import r48.RubyIO;
import r48.schema.ISchemaElement;
import r48.schema.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIHHalfsplit;
import r48.ui.UIScrollVertLayout;

/**
 * Colours, tones, same thing
 * Created on 1/3/17.
 */
public class CTNativeSchemaElement implements ISchemaElement {
    public final String cls;
    public CTNativeSchemaElement(String c) {
        cls = c;
    }
    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        UIScrollVertLayout uiSVL = new UIScrollVertLayout();
        RubyCT rct = new RubyCT(target.userVal);
        addField(uiSVL, "R", 0, rct, path);
        addField(uiSVL, "G", 4, rct, path);
        addField(uiSVL, "B", 8, rct, path);
        addField(uiSVL, "A/L", 12, rct, path);
        uiSVL.setBounds(new Rect(0, 0, 128, 9 * 4));
        return uiSVL;
    }

    private void addField(UIScrollVertLayout uiSVL, String r, final int i, final RubyCT targ, final SchemaPath sp) {
        final UINumberBox uinb = new UINumberBox(false);
        uinb.number = (int) targ.innerTable.getDouble(i);
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
        uiSVL.panels.add(new UIHHalfsplit(1, 3, new UILabel(r, false), uinb));
    }

    @Override
    public int maxHoldingHeight() {
        return 9 * 4;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, 'u', setDefault)) {
            target.symVal = cls;
            target.userVal = new byte[32];
            RubyCT rct = new RubyCT(target.userVal);
            rct.innerTable.putDouble(0, 0);
            rct.innerTable.putDouble(4, 0);
            rct.innerTable.putDouble(8, 0);
            rct.innerTable.putDouble(12, 255);
            path.changeOccurred(true);
        }
    }
}
