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
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class BooleanSchemaElement extends SchemaElement {
    public boolean defaultVal = false;

    public BooleanSchemaElement(boolean defVal) {
        defaultVal = defVal;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final UITextButton utb = new UITextButton(FontSizes.schemaButtonTextHeight, determineTruth(target) ? TXDB.get("True") : TXDB.get("False"), null).togglable();
        utb.state = determineTruth(target);
        utb.OnClick = new Runnable() {
            @Override
            public void run() {
                modifyValueTruth(target, utb.state);
                path.changeOccurred(false);
                utb.Text = utb.state ? TXDB.get("True") : TXDB.get("False");
            }
        };
        return utb;
    }

    public boolean determineTruth(RubyIO rubyIO) {
        return rubyIO.type == 'T';
    }

    public boolean modifyValueTruth(RubyIO target, boolean truth) {
        int lastType = target.type;
        target.type = truth ? 'T' : 'F';
        return lastType != target.type;
    }

    public boolean truthInvalid(RubyIO target) {
        if (target.type == 'T')
            return false;
        if (target.type == 'F')
            return false;
        return true;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        boolean modified = false;
        if (setDefault) {
            modified = modifyValueTruth(target, defaultVal);
        } else if (truthInvalid(target)) {
            modified = modifyValueTruth(target, defaultVal);
        }
        if (modified)
            path.changeOccurred(true);
    }

}
