/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * Created on 12/29/16.
 */
public class IVarSchemaElement extends SchemaElement {
    public String iVar;
    public SchemaElement subElem;
    public boolean optional = false;

    public boolean fieldWidthOverride = false;
    public int fieldWidth;

    public IVarSchemaElement(String iv, SchemaElement sub, boolean opt) {
        iVar = iv;
        subElem = sub;
        optional = opt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UILabel uil = new UILabel(iVar + " ", FontSizes.schemaFieldTextHeight);
        if (fieldWidthOverride) {
            uil.setBounds(new Rect(0, 0, fieldWidth, uil.getBounds().height));
            fieldWidthOverride = false;
        }
        RubyIO tgo = target.getInstVarBySymbol(iVar);
        UIElement e2;
        if (tgo == null) {
            if (!optional)
                throw new RuntimeException("Error: Made it to IVarSchemaElement.buildHoldingEditor when the actual IVar in question was missing, " + iVar);
            e2 = new UITextButton(FontSizes.schemaButtonTextHeight, "<Missing - add?>", new Runnable() {
                @Override
                public void run() {
                    if (!target.iVars.containsKey(iVar)) {
                        createIVar(target, path, false);
                    }
                }
            });
        } else {
            e2 = subElem.buildHoldingEditor(tgo, launcher, path.otherIndex("." + iVar));
            if (optional)
                e2 = new UIAppendButton("-", e2, new Runnable() {
                    @Override
                    public void run() {
                        if (target.iVars.containsKey(iVar)) {
                            target.iVars.remove(iVar);
                            path.changeOccurred(false);
                        }
                    }
                }, FontSizes.schemaButtonTextHeight);
        }
        final UIElement elem = e2;
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
        return UILabel.getRecommendedSize(iVar + " ", FontSizes.schemaFieldTextHeight).width;
    }

    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
    }

    @Override
    public int maxHoldingHeight() {
        int h = Math.max(UILabel.getRecommendedSize("", FontSizes.schemaFieldTextHeight).height, UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height);
        if (subElem.maxHoldingHeight() > h)
            return subElem.maxHoldingHeight();
        return h;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (target.iVars.containsKey(iVar)) {
            RubyIO r = target.iVars.get(iVar);
            subElem.modifyVal(r, path, setDefault);
        } else {
            if (!optional)
                createIVar(target, path, true);
        }
    }

    private void createIVar(RubyIO target, SchemaPath targetPath, boolean mv) {
        RubyIO r = new RubyIO();
        // being created, so create from scratch no matter what.
        subElem.modifyVal(r, targetPath.otherIndex(iVar), mv);
        target.iVars.put(iVar, r);
        targetPath.changeOccurred(mv);
    }
}
