/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.schema.ISchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIHelpSystem;

/**
 * Help Window Schema Element.
 * Used in places where things just don't explain themselves.
 * This is view-only, and doesn't do much - just embeds a help system control with the page number set from an instance variable.
 * Created on 2/15/17.
 */
public class HWNDSchemaElement implements ISchemaElement {
    public final String ivar, file;
    public HWNDSchemaElement(String iv, String f) {
        ivar = iv;
        file = f;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        UIHelpSystem uhs = new UIHelpSystem(null, null, file);
        uhs.loadPage((int) target.getInstVarBySymbol(ivar).fixnumVal);
        return uhs;
    }

    @Override
    public int maxHoldingHeight() {
        // IDK.
        return 100;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
    }
}
