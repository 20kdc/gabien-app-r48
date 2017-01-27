/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.*;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class IVarSchemaElement implements ISchemaElement {
    public String iVar;
    public ISchemaElement subElem;

    public boolean fieldWidthOverride = false;
    public int fieldWidth;

    public IVarSchemaElement(String iv, ISchemaElement sub) {
        iVar = iv;
        subElem = sub;
    }
    @Override
    public UIElement buildHoldingEditor(RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UILabel uil = new UILabel(iVar, false);
        if (fieldWidthOverride) {
            uil.setBounds(new Rect(0, 0, fieldWidth, uil.getBounds().height));
            fieldWidthOverride = false;
        }
        RubyIO tgo = target.getInstVarBySymbol(iVar);
        if (tgo == null)
            throw new RuntimeException("Error: Made it to IVarSchemaElement.buildHoldingEditor when the actual IVar in question was missing, " + iVar);
        final UIElement elem = subElem.buildHoldingEditor(tgo, launcher, path.otherIndex("." + iVar));
        final UIPanel panel = new UIPanel() {
            @Override
            public void setBounds(Rect r) {
                super.setBounds(r);
                // just leave the bounds as-is on UIL, but...
                int lw = uil.getBounds().width;
                elem.setBounds(new Rect(lw, 0, r.width - lw, r.height));
            }
        };
        panel.allElements.add(uil);
        panel.allElements.add(elem);
        panel.setBounds(new Rect(0, 0, 128, maxHoldingHeight()));
        return panel;
    }

    public int getDefaultFieldWidth() {
        return new UILabel(iVar, false).getBounds().width;
    }

    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
    }

    @Override
    public int maxHoldingHeight() {
        if (subElem.maxHoldingHeight() > 9)
            return subElem.maxHoldingHeight();
        return 9;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (target.iVars.containsKey(iVar)) {
            RubyIO r = target.iVars.get(iVar);
            subElem.modifyVal(r, path, setDefault);
        } else {
            RubyIO r = new RubyIO();
            // being created, so create from scratch no matter what.
            subElem.modifyVal(r, path.otherIndex(iVar), true);
            target.iVars.put(iVar, r);
            path.changeOccurred(true);
        }
    }
}
