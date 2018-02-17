/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * Created on 9 October 2017 from the ashes of IVarSchemaElement
 */
public class PathSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public String pStr;
    public String alias;
    public SchemaElement subElem;
    public boolean optional = false;

    private boolean fieldWidthOverride = false;
    private int fieldWidth;

    public PathSchemaElement(String iv, String a, SchemaElement sub, boolean opt) {
        pStr = iv;
        alias = a;
        subElem = sub;
        optional = opt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UILabel uil = new ArrayElementSchemaElement.UIOverridableWidthLabel(alias + " ", FontSizes.schemaFieldTextHeight, fieldWidth, fieldWidthOverride);
        fieldWidthOverride = false;
        RubyIO tgo = PathSyntax.parse(target, pStr, 0);
        UIElement e2;
        if (tgo == null) {
            if (!optional)
                throw new RuntimeException("Error: Made it to PathSchemaElement.buildHoldingEditor when target wasn't there: " + pStr);
            e2 = new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("<Missing - add?>"), new Runnable() {
                @Override
                public void run() {
                    RubyIO rio = PathSyntax.parse(target, pStr, 1);
                    if (rio.type == 0)
                        createIVar(rio, path, false);
                }
            });
        } else {
            e2 = subElem.buildHoldingEditor(tgo, launcher, path.otherIndex(alias));
            if (optional)
                e2 = new UIAppendButton("-", e2, new Runnable() {
                    @Override
                    public void run() {
                        if (PathSyntax.parse(target, pStr, 2) != null)
                            path.changeOccurred(false);
                    }
                }, FontSizes.schemaButtonTextHeight);
        }
        return new UISplitterLayout(uil, e2, false, 0);
    }

    @Override
    public int getDefaultFieldWidth(RubyIO target) {
        return UILabel.getRecommendedTextSize(alias + " ", FontSizes.schemaFieldTextHeight).width;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        RubyIO r = PathSyntax.parse(target, pStr, 0);
        if (r != null) {
            subElem.modifyVal(r, path.otherIndex(alias), setDefault);
        } else {
            if (!optional) {
                RubyIO rio = PathSyntax.parse(target, pStr, 1);
                if (rio.type == 0)
                    createIVar(rio, path, true);
            }
        }
    }

    private void createIVar(RubyIO r, SchemaPath targetPath, boolean mv) {
        // being created, so create from scratch no matter what.
        subElem.modifyVal(r, targetPath.otherIndex(alias), mv);
        targetPath.changeOccurred(mv);
    }
}
