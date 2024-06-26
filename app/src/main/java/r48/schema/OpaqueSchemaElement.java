/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.App;
import r48.UITest;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/28/16.
 */
public class OpaqueSchemaElement extends SchemaElement.Leaf {
    public OpaqueSchemaElement(App app) {
        super(app);
    }

    public String getMessage(IRIO v) {
        return T.s.cantEdit + v;
    }

    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(getMessage(target), app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                launcher.launchOther(new UITest(launcher.getApp(), target, path.root));
            }
        });
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // Not a clue.
        if (setDefault)
            path.changeOccurred(true);
    }
}
