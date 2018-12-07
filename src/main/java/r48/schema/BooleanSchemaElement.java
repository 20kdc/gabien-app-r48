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
import r48.dbs.TXDB;
import r48.io.data.IRIO;
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
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        final UITextButton utb = new UITextButton(determineTruth(target) ? TXDB.get("True") : TXDB.get("False"), FontSizes.schemaFieldTextHeight, null).togglable(determineTruth(target));
        utb.onClick = new Runnable() {
            @Override
            public void run() {
                modifyValueTruth(target, utb.state);
                path.changeOccurred(false);
            }
        };
        return utb;
    }

    public boolean determineTruth(IRIO rubyIO) {
        return rubyIO.getType() == 'T';
    }

    public boolean modifyValueTruth(IRIO target, boolean truth) {
        int lastType = target.getType();
        target.setBool(truth);
        return lastType != target.getType();
    }

    public boolean truthInvalid(IRIO target) {
        int t = target.getType();
        if (t == 'T')
            return false;
        if (t == 'F')
            return false;
        return true;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
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
