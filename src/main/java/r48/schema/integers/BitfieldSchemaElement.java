/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.integers;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyCT;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.concurrent.atomic.AtomicReference;

/**
 * BitfieldTableCellEditor as a schema element
 * Created on Sep 19 2017
 */
public class BitfieldSchemaElement extends IntegerSchemaElement {
    public final String[] flags;
    public BitfieldSchemaElement(int def, String[] f) {
        super(def);
        flags = f;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(path, launcher, this, target);
        BitfieldTableCellEditor.installEditor(flags, new IConsumer<UIElement>() {
            @Override
            public void accept(UIElement element) {
                uiSVL.panels.add(element);
            }
        }, new AtomicReference<IConsumer<Integer>>(new IConsumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                target.fixnumVal = integer;
                path.changeOccurred(false);
            }
        })).accept((int) target.fixnumVal);
        uiSVL.panels.add(new UILabel(TXDB.get("Manual Edit:"), FontSizes.tableElementTextHeight));
        uiSVL.panels.add(super.buildHoldingEditor(target, launcher, path));
        return uiSVL;
    }
}
