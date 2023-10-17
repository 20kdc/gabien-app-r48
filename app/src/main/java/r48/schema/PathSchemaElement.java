/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEnUI;
import gabien.ui.UIBorderedElement;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;

/**
 * Created on 9 October 2017 from the ashes of IVarSchemaElement
 */
public class PathSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public final PathSyntax pStr;
    public @Nullable FF0 alias;
    public SchemaElement subElem;
    public boolean optional;

    private boolean fieldWidthOverride = false;
    private int fieldWidth;

    public PathSchemaElement(@NonNull PathSyntax iv, @Nullable FF0 a, @NonNull SchemaElement sub, boolean opt) {
        super(sub.app);
        pStr = iv;
        alias = a;
        subElem = sub;
        optional = opt;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        UILabel uil = null;
        if (alias != null)
            uil = new UILabel(alias.r() + " ", app.f.schemaFieldTH);
        IRIO tgo = pStr.get(target);
        UIElement e2;
        if (tgo == null) {
            if (!optional)
                throw new RuntimeException("Error: Made it to PathSchemaElement.buildHoldingEditor when target wasn't there: " + pStr);
            e2 = new UITextButton(T.s.bOptAdd, app.f.schemaFieldTH, new Runnable() {
                @Override
                public void run() {
                    IRIO rio = pStr.add(target);
                    createIVar(rio, path, false);
                }
            });
        } else {
            e2 = subElem.buildHoldingEditor(tgo, launcher, path.otherIndex(alias.r()));
            if (optional)
                e2 = new UIAppendButton("-", e2, new Runnable() {
                    @Override
                    public void run() {
                        if (pStr.del(target) != null)
                            path.changeOccurred(false);
                    }
                }, app.f.schemaFieldTH);
        }
        if (uil != null) {
            UIFieldLayout usl = new UIFieldLayout(uil, e2, fieldWidth, fieldWidthOverride);
            fieldWidthOverride = false;
            return usl;
        }
        return e2;
    }

    @Override
    public int getDefaultFieldWidth(IRIO target) {
        if (alias != null)
            return UIBorderedElement.getRecommendedTextSize(GaBIEnUI.sysThemeRoot.getTheme(), alias.r() + " ", app.f.schemaFieldTH).width;
        return 0;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        fieldWidth = w;
        fieldWidthOverride = true;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        IRIO r = pStr.get(target);
        if (r != null) {
            subElem.modifyVal(r, path.otherIndex(alias.r()), setDefault);
        } else {
            if (!optional) {
                IRIO rio = pStr.add(target);
                if (rio == null)
                    throw new RuntimeException("failed create during modifyVal, " + pStr);
                createIVar(rio, path, true);
            }
        }
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        IRIO r = pStr.get(target);
        if (r != null)
            subElem.visit(r, detailedPaths ? path.otherIndex(alias.r()) : path, v, detailedPaths);
    }

    private void createIVar(IRIO r, SchemaPath targetPath, boolean mv) {
        // being created, so create from scratch no matter what.
        subElem.modifyVal(r, targetPath.otherIndex(alias.r()), mv);
        targetPath.changeOccurred(mv);
    }
}
