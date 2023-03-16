/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.displays;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.App;
import r48.io.data.IRIO;
import r48.schema.HiddenSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF2;

/**
 * Created on 1st August 2022.
 */
public class WindowTitleAttachmentSchemaElement extends SchemaElement {
    public final FF2 suffixRoutine;

    public WindowTitleAttachmentSchemaElement(App app, FF2 sr) {
        super(app);
        suffixRoutine = sr;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return HiddenSchemaElement.makeHiddenElement();
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
    }

    @Override
    public @Nullable String windowTitleSuffix(SchemaPath path) {
        return suffixRoutine.r(path.targetElement, path.lastArrayIndex);
    }
}
