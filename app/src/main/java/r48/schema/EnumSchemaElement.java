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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum. There was something here about it being important to switch into a new view, but in practice stuff changed.
 * The system is a lot cleaner now it's having the entire UI rebuilt all the time.
 * Created on 12/30/16.
 */
public class EnumSchemaElement extends SchemaElement {
    // Maps ValueSyntax strings to option text
    public final HashMap<String, UIEnumChoice.Option> lookupOptions = new HashMap<String, UIEnumChoice.Option>();
    // Options for use in enum choice dialogs
    public final LinkedList<UIEnumChoice.Option> viewOptions = new LinkedList<UIEnumChoice.Option>();

    public String buttonText;
    public UIEnumChoice.EntryMode entryMode;
    public IRIO defaultVal;

    public EnumSchemaElement(HashMap<String, String> o, IRIO def, UIEnumChoice.EntryMode em, String bt) {
        for (Map.Entry<String, String> mapping : o.entrySet())
            lookupOptions.put(mapping.getKey(), makeStandardOption(ValueSyntax.decode(mapping.getKey()), mapping.getValue(), null));
        convertLookupToView();
        // continue
        entryMode = em;
        buttonText = bt;
        defaultVal = def;
    }

    public EnumSchemaElement(Collection<UIEnumChoice.Option> opts, IRIO def, UIEnumChoice.EntryMode em, String bt) {
        viewOptions.addAll(opts);
        Collections.sort(viewOptions, UIEnumChoice.COMPARATOR_OPTION);
        convertViewToLookup();
        // continue
        entryMode = em;
        buttonText = bt;
        defaultVal = def;
    }

    public void convertLookupToView() {
        viewOptions.clear();
        viewOptions.addAll(lookupOptions.values());
        Collections.sort(viewOptions, UIEnumChoice.COMPARATOR_OPTION);
    }

    public void convertViewToLookup() {
        lookupOptions.clear();
        for (UIEnumChoice.Option o : viewOptions)
            lookupOptions.put(ValueSyntax.encode(o.value), o);
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

    public static UIEnumChoice.Option makeStandardOption(RubyIO val, String text, @Nullable IConsumer<String> edit) {
        return new UIEnumChoice.Option(val.toString() + " : ", text, val, edit);
    }

    public String viewValue(IRIO val, boolean prefix) {
        String v2 = ValueSyntax.encode(val);
        if (v2 != null) {
            UIEnumChoice.Option st = lookupOptions.get(v2);
            if (st != null) {
                if (!prefix)
                    return st.textSuffix;
                return st.textMerged;
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
