/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;

/**
 * Help Window Schema Element.
 * Used in places where things just don't explain themselves.
 * This is view-only, and doesn't do much - just embeds a help system control with the page number set from an instance variable.
 * Created on 2/15/17.
 */
public class HWNDSchemaElement extends SchemaElement {
    public final String ivar, file;

    public HWNDSchemaElement(String iv, String f) {
        ivar = iv;
        file = f;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        UIHelpSystem uhs = new UIHelpSystem();
        final HelpSystemController hsc = new HelpSystemController(null, file, uhs);
        uhs.onLinkClick = new IConsumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                AppMain.startHelp(integer);
            }
        };
        if (ivar != null) {
            hsc.loadPage((int) target.getInstVarBySymbol(ivar).fixnumVal);
        } else{
            hsc.loadPage(0);
        }
        return uhs;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
    }
}
