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
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIEnumChoice;

import java.util.HashMap;

/**
 * A copy of EnumSchemaElement that handles non-integer elements.
 * Created on 12/30/16.
 */
public class SymEnumSchemaElement extends SchemaElement {
    public String[] options;
    public boolean actuallyString;
    public HashMap<String, Integer> viewOptions;

    public SymEnumSchemaElement(String[] on, String[] o, boolean actStr) {
        options = o;
        actuallyString = actStr;
        viewOptions = new HashMap<String, Integer>();
        for (int i = 0; i < on.length; i++)
            viewOptions.put(on[i], i);
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        String bText = target.symVal;
        if (actuallyString)
            bText = target.decString();
        return new UITextButton(FontSizes.schemaButtonTextHeight, bText, new Runnable() {
            @Override
            public void run() {
                launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer < 0)
                            return;
                        if (integer >= options.length)
                            return;
                        if (actuallyString) {
                            target.encString(options[integer], false);
                        } else {
                            target.symVal = options[integer];
                        }
                        path.changeOccurred(false);
                        // Enums can affect parent format, so deal with that now.
                        launcher.switchObject(path.findBack());
                    }
                }, viewOptions, ""), null, path), target));
            }
        });
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (actuallyString) {
            if (IntegerSchemaElement.ensureType(target, '"', setDefault)) {
                target.encString(options[0], false);
                path.changeOccurred(true);
            }
        } else {
            if (IntegerSchemaElement.ensureType(target, ':', setDefault)) {
                target.symVal = options[0];
                path.changeOccurred(true);
            }
        }
    }
}
