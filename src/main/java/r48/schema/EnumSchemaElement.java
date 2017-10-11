/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIEnumChoice;

import java.util.HashMap;

/**
 * Enum. There was something here about it being important to switch into a new view, but in practice stuff changed.
 * The system is a lot cleaner now it's having the entire UI rebuilt all the time.
 * Created on 12/30/16.
 */
public class EnumSchemaElement extends SchemaElement {
    public HashMap<Integer, String> options;
    public HashMap<String, Integer> viewOptions;
    public String buttonText;
    public int defaultVal;

    public EnumSchemaElement(HashMap<Integer, String> o, int def, String bt) {
        options = o;
        viewOptions = new HashMap<String, Integer>();
        for (Integer si : options.keySet())
            viewOptions.put(viewValue(si, true), si);
        buttonText = bt;
        defaultVal = def;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, viewValue((int) target.fixnumVal, true), new Runnable() {
            @Override
            public void run() {
                launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        target.fixnumVal = integer;
                        path.changeOccurred(false);
                        // Enums can affect parent format, so deal with that now.
                        launcher.switchObject(path.findBack());
                    }
                }, viewOptions, buttonText), null, path), target));
            }
        });
    }

    public String viewValue(int fixnumVal, boolean prefix) {
        String st = options.get(fixnumVal);
        // Maybe formatstring this - it has side effects in text formatting system
        if (st == null)
            return TXDB.get("int:") + fixnumVal;
        if (!prefix)
            return st;
        return fixnumVal + ":" + st;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, 'i', setDefault)) {
            target.fixnumVal = defaultVal;
            path.changeOccurred(true);
        }
    }
}
