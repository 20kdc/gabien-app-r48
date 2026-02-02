/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UINumberBox;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.io.MemoryishRW;
import r48.R48;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Colours, tones, same thing
 * Created on 1/3/17.
 */
public class CTNativeSchemaElement extends SchemaElement.Leaf {
    public final String cls;
    public final EmbedDataKey<Double> scrollPointKey = new EmbedDataKey<>();

    public CTNativeSchemaElement(R48 app, String c) {
        super(app);
        cls = c;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        MemoryishRW rct = target.editUser();
        UIElement[] uiSVLContents = {
                addField(T.s.toneR, 0, rct, path),
                addField(T.s.toneG, 8, rct, path),
                addField(T.s.toneB, 16, rct, path),
                addField(T.s.toneAL, 24, rct, path)
        };
        return AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target, uiSVLContents);
    }

    private UIElement addField(String r, final int i, final MemoryishRW targ, final SchemaPath sp) {
        final UINumberBox uinb = new UINumberBox((long) targ.getF64LE(i), app.f.schemaFieldTH);
        uinb.onEdit = () -> {
            if (cls.equals("Tone")) {
                if (uinb.getNumber() < -255)
                    uinb.setNumber(-255);
            } else {
                if (uinb.getNumber() < 0)
                    uinb.setNumber(0);
            }
            if (uinb.getNumber() > 255)
                uinb.setNumber(255);;
            targ.setF64LE(i, uinb.getNumber());
            sp.changeOccurred(false);
        };
        return new UISplitterLayout(new UILabel(r, app.f.schemaFieldTH), uinb, false, 1, 3);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (checkType(target, 'u', cls, setDefault)) {
            ByteArrayMemoryish bam = new ByteArrayMemoryish(new byte[32]);
            bam.setF64LE(0, 0);
            bam.setF64LE(8, 0);
            bam.setF64LE(16, 0);
            bam.setF64LE(24, 255);
            target.setUser(cls, bam.data);
            path.changeOccurred(true);
        }
    }
}
