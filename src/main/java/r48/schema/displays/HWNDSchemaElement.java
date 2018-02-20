/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.PathSyntax;
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
    public final boolean sdb2;

    public HWNDSchemaElement(String iv, String f, boolean sdb2x) {
        ivar = iv;
        file = f;
        sdb2 = sdb2x;
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
            hsc.loadPage((int) PathSyntax.parse(target, ivar, sdb2).fixnumVal);
        } else {
            hsc.loadPage(0);
        }
        return uhs;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
    }
}
