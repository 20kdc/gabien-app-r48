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
import gabien.ui.UIScrollLayout;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Basically a UI element masquerading as a schema element.
 * Created on 12/29/16.
 */
public class AggregateSchemaElement extends SchemaElement {
    public LinkedList<SchemaElement> aggregate = new LinkedList<SchemaElement>();

    public AggregateSchemaElement(SchemaElement[] ag) {
        Collections.addAll(aggregate, ag);
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        UIScrollLayout uiSVL = new UIScrollLayout(true);
        // Help IVarSchemaElements along a little
        int maxFW = 1;
        for (SchemaElement ise : aggregate) {
            if (ise instanceof IVarSchemaElement) {
                int dfw = ((IVarSchemaElement) ise).getDefaultFieldWidth();
                if (maxFW < dfw)
                    maxFW = dfw;
            }
        }
        for (SchemaElement ise : aggregate) {
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
        for (SchemaElement ise : aggregate)
            i += ise.maxHoldingHeight();
        return i;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath i, boolean setDefault) {
        for (SchemaElement ise : aggregate)
            ise.modifyVal(target, i, setDefault);
    }
}
