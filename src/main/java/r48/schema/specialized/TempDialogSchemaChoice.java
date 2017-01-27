/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;
import r48.schema.ISchemaElement;

/**
 * Used to create dialogs within the system.
 * The older system was arguably cleaner, but UIElements couldn't be refreshed,
 *  which left things like button texts stale.
 * Created on 12/30/16.
 */
public class TempDialogSchemaChoice implements ISchemaElement {
    public UIElement heldDialog;
    public SchemaPath hPar;
    public TempDialogSchemaChoice(UIElement held, SchemaPath hr) {
        heldDialog = held;
        hPar = hr;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return heldDialog;
    }

    @Override
    public int maxHoldingHeight() {
        throw new RuntimeException("Temporary dialog element.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        hPar.editor.modifyVal(hPar.targetElement, hPar, setDefault);
    }
}
