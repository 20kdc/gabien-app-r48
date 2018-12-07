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
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
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
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        UIHelpSystem uhs = new UIHelpSystem();
        final HelpSystemController hsc = new HelpSystemController(null, file, uhs);
        uhs.onLinkClick = new IConsumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                AppMain.startHelp(integer);
            }
        };
        if (ivar != null) {
            hsc.loadPage((int) PathSyntax.parse(target, ivar).getFX());
        } else {
            hsc.loadPage(0);
        }
        return uhs;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
    }
}
