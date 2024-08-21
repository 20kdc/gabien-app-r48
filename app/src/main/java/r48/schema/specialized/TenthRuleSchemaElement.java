/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.App;
import r48.io.data.IRIO;
import r48.minivm.fn.MVMFn;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF1;

/**
 * Literally a button.
 * Created 21st August, 2024.
 */
public class TenthRuleSchemaElement extends SchemaElement.Leaf {
    public final FF1 text;
    public final MVMFn executeFn;
    public final EmbedDataKey<Boolean> buttonEDKey = new EmbedDataKey<>();

    public TenthRuleSchemaElement(App app, FF1 iT, MVMFn iF) {
        super(app);
        text = iT;
        executeFn = iF;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextButton importer = new UITextButton(text.r(target), app.f.schemaFieldTH, () -> {
            try {
                executeFn.clDirect(target, launcher, path);
            } catch (Exception ioe) {
                app.ui.launchDialog(ioe);
            }
        });

        AggregateSchemaElement.hookButtonForPressPreserve(launcher, target, importer, buttonEDKey);

        return importer;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {

    }
}
