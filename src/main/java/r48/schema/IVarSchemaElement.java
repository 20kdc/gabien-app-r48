/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * Created on 12/29/16.
 */
public class IVarSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public String iVar;
    public String alias;
    public SchemaElement subElem;
    public boolean optional = false;

    private boolean fieldWidthOverride = false;
    private int fieldWidth;

    public IVarSchemaElement(String iv, String a, SchemaElement sub, boolean opt) {
        iVar = iv;
        alias = a;
        subElem = sub;
        optional = opt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UILabel uil = new UILabel(alias + " ", FontSizes.schemaFieldTextHeight);
        if (fieldWidthOverride) {
            uil.setBounds(new Rect(0, 0, fieldWidth, uil.getBounds().height));
            fieldWidthOverride = false;
        }
        RubyIO tgo = target.getInstVarBySymbol(iVar);
        UIElement e2;
        if (tgo == null) {
            if (!optional)
                throw new RuntimeException("Error: Made it to IVarSchemaElement.buildHoldingEditor when the actual IVar in question was missing, " + iVar);
            e2 = new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("<Missing - add?>"), new Runnable() {
                @Override
                public void run() {
                    if (target.getInstVarBySymbol(iVar) == null) {
                        createIVar(target, path, false);
                    }
                }
            });
        } else {
            e2 = subElem.buildHoldingEditor(tgo, launcher, path.otherIndex(alias));
            if (optional)
                e2 = new UIAppendButton("-", e2, new Runnable() {
                    @Override
                    public void run() {
                        if (target.getInstVarBySymbol(iVar) != null) {
                            target.rmIVar(iVar);
                            path.changeOccurred(false);
                        }
                    }
                }, FontSizes.schemaButtonTextHeight);
        }
        return new UISplitterLayout(uil, e2, false, 0);
    }

    @Override
    public int getDefaultFieldWidth(RubyIO target) {
        return UILabel.getRecommendedSize(alias + " ", FontSizes.schemaFieldTextHeight).width;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (target.getInstVarBySymbol(iVar) != null) {
            RubyIO r = target.getInstVarBySymbol(iVar);
            subElem.modifyVal(r, path.otherIndex(alias), setDefault);
        } else {
            if (!optional)
                createIVar(target, path, true);
        }
    }

    private void createIVar(RubyIO target, SchemaPath targetPath, boolean mv) {
        RubyIO r = new RubyIO();
        // being created, so create from scratch no matter what.
        subElem.modifyVal(r, targetPath.otherIndex(alias), mv);
        target.addIVar(iVar, r);
        targetPath.changeOccurred(mv);
    }
}
