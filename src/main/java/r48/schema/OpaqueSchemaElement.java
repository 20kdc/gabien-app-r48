/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/28/16.
 */
public class OpaqueSchemaElement extends SchemaElement {

    public String getMessage(RubyIO v) {
        return TXDB.get("Can't edit: ") + v;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(getMessage(target), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                launcher.launchOther(new UITest(target));
            }
        });
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // Not a clue, so re-initialize if all else fails.
        if ((target.type == 0) || setDefault) {
            target.setNull();
            path.changeOccurred(true);
        }
    }
}
