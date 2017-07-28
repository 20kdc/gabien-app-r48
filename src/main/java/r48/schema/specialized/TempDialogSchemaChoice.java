/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Used to create dialogs within the system.
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
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        if (update != null)
            update.run();
        return heldDialog;
    }

    @Override
    public int maxHoldingHeight() {
        throw new RuntimeException("Temporary dialog element.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        hPar.editor.modifyVal(hPar.targetElement, hPar, setDefault);
        if (update != null)
            update.run();
    }
}
