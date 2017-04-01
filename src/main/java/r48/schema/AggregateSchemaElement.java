/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIScrollVertLayout;

import java.util.LinkedList;

/**
 * Basically a UI element masquerading as a schema element.
 * Created on 12/29/16.
 */
public class AggregateSchemaElement implements ISchemaElement {
    public LinkedList<ISchemaElement> aggregate = new LinkedList<ISchemaElement>();

    public AggregateSchemaElement(ISchemaElement[] ag) {
        for (ISchemaElement ise : ag)
            aggregate.add(ise);
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        UIScrollVertLayout uiSVL = new UIScrollVertLayout() {
            @Override
            public String toString() {
                return "SCHEMA Obj.";
            }
        };
        // Help IVarSchemaElements along a little
        int maxFW = 1;
        for (ISchemaElement ise : aggregate) {
            if (ise instanceof IVarSchemaElement) {
                int dfw = ((IVarSchemaElement) ise).getDefaultFieldWidth();
                if (maxFW < dfw)
                    maxFW = dfw;
            }
        }
        for (ISchemaElement ise : aggregate) {
            if (ise instanceof IVarSchemaElement)
                ((IVarSchemaElement) ise).setFieldWidthOverride(maxFW);
            uiSVL.panels.add(ise.buildHoldingEditor(target, launcher, path));
        }
        uiSVL.setBounds(new Rect(0, 0, 128, maxHoldingHeight()));
        return uiSVL;
    }

    @Override
    public int maxHoldingHeight() {
        // Give a value which won't result in a scroller.
        int i = 0;
        for (ISchemaElement ise : aggregate)
            i += ise.maxHoldingHeight();
        return i;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath i, boolean setDefault) {
        for (ISchemaElement ise : aggregate)
            ise.modifyVal(target, i, setDefault);
    }
}
