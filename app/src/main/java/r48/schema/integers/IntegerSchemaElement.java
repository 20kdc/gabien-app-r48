/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.integers;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import gabien.ui.UINumberBox;
import gabien.ui.UIScrollLayout;
import r48.App;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * IntegerSchemaElement is a type of schema element that can be used outside of a schema context.
 * Thus, buildHoldingEditor has been finaled.
 * Created on 12/29/16.
 */
public class IntegerSchemaElement extends SchemaElement.Leaf {
    public long defaultInt;

    public IntegerSchemaElement(App app, long i) {
        super(app);
        defaultInt = i;
    }

    @Override
    public final UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        return buildIntegerEditor(target.getFX(), new IIntegerContext() {
            @Override
            public void update(long n) {
                target.setFX(filter(n));
                path.changeOccurred(false);
            }

            @Override
            public UIScrollLayout newSVL() {
                return AggregateSchemaElement.createScrollSavingSVL(launcher, IntegerSchemaElement.this, target);
            }
        }).uie;
    }

    public ActiveInteger buildIntegerEditor(long oldVal, final IntegerSchemaElement.IIntegerContext context) {
        final UINumberBox unb = new UINumberBox(oldVal, app.f.schemaFieldTH);
        unb.readOnly = isReadOnly();
        unb.onEdit = new Runnable() {
            @Override
            public void run() {
                context.update(unb.number);
            }
        };
        return new ActiveInteger(unb, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                unb.number = aLong;
            }
        });
    }

    public boolean isReadOnly() {
        return false;
    }

    public long filter(long i) {
        return i;
    }

    @Override
    public final void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (checkType(target, 'i', null, setDefault)) {
            target.setFX(defaultInt);
            path.changeOccurred(true);
        }
        // It may, or may not, be a good idea to filter the value here.
        // In any case, I have at least moved the usage of filter to the outer structure,
        //  so that it is always in use.
    }

    public interface IIntegerContext {
        // Causes UI rebuild.
        void update(long n);

        UIScrollLayout newSVL();
    }

    public final class ActiveInteger {
        public final UIElement uie;
        public final Consumer<Long> onValueChange;

        public ActiveInteger(UIElement u, Consumer<Long> ovc) {
            uie = u;
            onValueChange = ovc;
        }
    }
}
