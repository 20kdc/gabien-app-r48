/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.integers;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.AggregateSchemaElement;
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
