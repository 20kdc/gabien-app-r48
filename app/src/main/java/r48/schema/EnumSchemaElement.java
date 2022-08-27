/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.ValueSyntax;
import r48.io.data.IRIO;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.UIEnumChoice;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Enum. There was something here about it being important to switch into a new view, but in practice stuff changed.
 * The system is a lot cleaner now it's having the entire UI rebuilt all the time.
 * Created on 12/30/16.
 */
public class EnumSchemaElement extends SchemaElement {
    // Maps ValueSyntax strings to option text
    public HashMap<String, String> options;
    // Maps option text to output RubyIOs
    private LinkedList<UIEnumChoice.Option> viewOptions;

    public String buttonText;
    public UIEnumChoice.EntryMode entryMode;
    public IRIO defaultVal;

    public EnumSchemaElement(HashMap<String, String> o, IRIO def, String es) {
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
        viewOptions = new LinkedList<UIEnumChoice.Option>();
        for (String si : options.keySet()) {
            RubyIO dec = ValueSyntax.decode(si);
            viewOptions.add(new UIEnumChoice.Option(viewValue(dec, true), dec));
        }
        Collections.sort(viewOptions, new Comparator<UIEnumChoice.Option>() {
            public int compare(UIEnumChoice.Option o1, UIEnumChoice.Option o2) {
                return UITest.natStrComp(o1.textMerged, o2.textMerged);
            }
        });
    }

    // Overridden by variants that update whenever needed.
    // Note that using this has significant overhead.
    public void liveUpdate() {

    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(viewValue(target, true), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                liveUpdate();
                launcher.pushObject(path.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<RubyIO>() {
                    @Override
                    public void accept(RubyIO integer) {
                        target.setDeepClone(integer);
                        path.changeOccurred(false);
                        // Enums can affect parent format, so deal with that now.
                        launcher.popObject();
                    }
                }, viewOptions, buttonText, entryMode), null, path), target));
            }
        });
    }

    public String viewValue(IRIO val, boolean prefix) {
        String v2 = ValueSyntax.encode(val);
        if (v2 != null) {
            String st = options.get(v2);
            if (st != null) {
                if (!prefix)
                    return st;
                return val + " : " + st;
            }
        }
        return val.toString();
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        liveUpdate();
        if (SchemaElement.checkType(target, defaultVal.getType(), null, setDefault)) {
            target.setDeepClone(defaultVal);
            path.changeOccurred(true);
        }
    }
}
