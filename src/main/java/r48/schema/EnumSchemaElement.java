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
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.dbs.ValueSyntax;
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
    // Maps ValueSyntax strings to option text
    public HashMap<String, String> options;
    // Maps option text to output RubyIOs
    public HashMap<String, RubyIO> viewOptions;

    public String buttonText;
    public UIEnumChoice.EntryMode entryMode;
    public RubyIO defaultVal;

    public EnumSchemaElement(HashMap<String, String> o, RubyIO def, String es) {
        options = o;
        if (es.contains(":")) {
            int i = es.indexOf(":");
            buttonText = es.substring(i + 1);
            es = es.substring(0, i);
            entryMode = UIEnumChoice.EntryMode.valueOf(es);
        } else {
            buttonText = es;
            entryMode = UIEnumChoice.EntryMode.INT;
        }
        convertOptions();
        defaultVal = def;
    }

    public void convertOptions() {
        viewOptions = new HashMap<String, RubyIO>();
        for (String si : options.keySet()) {
            RubyIO dec = ValueSyntax.decode(si, true);
            viewOptions.put(viewValue(dec, true), dec);
        }
    }

    // Overridden by variants that update whenever needed.
    // Note that using this has significant overhead.
    public void liveUpdate() {

    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(viewValue(target, true), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                liveUpdate();
                launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<RubyIO>() {
                    @Override
                    public void accept(RubyIO integer) {
                        target.setDeepClone(integer);
                        path.changeOccurred(false);
                        // Enums can affect parent format, so deal with that now.
                        launcher.switchObject(path.findBack());
                    }
                }, viewOptions, buttonText, entryMode), null, path), target));
            }
        });
    }

    public String viewValue(RubyIO val, boolean prefix) {
        String v2 = ValueSyntax.encode(val, true);
        if (v2 != null) {
            String st = options.get(v2);
            if (st != null) {
                if (!prefix)
                    return st;
                return FormatSyntax.formatExtended(TXDB.get("#A : #B"), val, new RubyIO().setString(st, true));
            }
        }
        return FormatSyntax.formatExtended(TXDB.get("#A (?)"), val);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        liveUpdate();
        if (SchemaElement.ensureType(target, (char) defaultVal.type, setDefault)) {
            target.setDeepClone(defaultVal);
            path.changeOccurred(true);
        }
    }
}
