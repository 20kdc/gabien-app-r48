/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Used to create dialogs within the system.
 * (NOTE: Schema hosts should refuse to clone dialogs containing this in the path. It *kind of* works. Emphasis on "kind of".)
 * The older system was arguably cleaner, but UIElements couldn't be refreshed,
 * which left things like button texts stale.
 * (The point of this object is that objects creating temporary dialogs create one of these as an excuse to launch a new panel,
 * and this forwards modifyVal requests back to the panel... I think.)
 * Created on 12/30/16.
 */
public class TempDialogSchemaChoice extends SchemaElement {
    public UIElement heldDialog;
    public Runnable update;
    public SchemaPath hPar;

    public TempDialogSchemaChoice(UIElement held, Runnable updater, SchemaPath hr) {
        heldDialog = held;
        update = updater;
        hPar = hr;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        if (update != null)
            update.run();
        return heldDialog;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (hPar.editor != null)
            hPar.editor.modifyVal(hPar.targetElement, hPar, setDefault);
        if (update != null)
            update.run();
    }
}
