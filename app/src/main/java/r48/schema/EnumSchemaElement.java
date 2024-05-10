/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.App;
import r48.dbs.ValueSyntax;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.ui.Art;
import r48.ui.UIAppendButton;
import r48.ui.dialog.UIEnumChoice;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum. There was something here about it being important to switch into a new view, but in practice stuff changed.
 * The system is a lot cleaner now it's having the entire UI rebuilt all the time.
 * Created on 12/30/16.
 */
public class EnumSchemaElement extends SchemaElement.Leaf {
    // Maps ValueSyntax strings to option text
    public final HashMap<String, UIEnumChoice.Option> lookupOptions = new HashMap<String, UIEnumChoice.Option>();
    // Options for use in enum choice dialogs
    public final LinkedList<UIEnumChoice.Option> viewOptions = new LinkedList<UIEnumChoice.Option>();

    /**
     * Default prefix disposition.
     */
    public boolean displayPrefixDefault;

    public FF0 buttonText;
    public UIEnumChoice.EntryMode entryMode;
    public DMKey defaultVal;

    public EnumSchemaElement(App app, HashMap<String, FF0> o, DMKey def, UIEnumChoice.EntryMode em, FF0 bt) {
        super(app);
        for (Map.Entry<String, FF0> mapping : o.entrySet())
            lookupOptions.put(mapping.getKey(), makeStandardOption(ValueSyntax.decode(mapping.getKey()), mapping.getValue(), null, null));
        convertLookupToView();
        // continue
        entryMode = em;
        buttonText = bt;
        defaultVal = def;
    }

    public EnumSchemaElement(App app, Collection<UIEnumChoice.Option> opts, DMKey def, UIEnumChoice.EntryMode em, FF0 bt) {
        super(app);
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
        final UIEnumChoice.Option opt = findOption(target);
        UITextButton button = new UITextButton(viewValue(target, Prefix.Prefix, opt), app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                liveUpdate();
                launcher.pushObject(path.newWindow(new TempDialogSchemaChoice(app, makeEnumChoiceDialog((integer) -> {
                    target.setDeepClone(integer);
                    path.changeOccurred(false);
                    // Enums can affect parent format, so deal with that now.
                    launcher.popObject();
                }), null, path), target));
            }
        });
        if (opt != null) {
            if (opt.furtherDataButton != null)
                return new UIAppendButton(Art.Symbol.CloneFrame.i(app), button, () -> {
                    launcher.newBlank().pushObject(opt.furtherDataButton);
                }, app.f.schemaFieldTH);
        }
        return button;
    }

    public UIEnumChoice makeEnumChoiceDialog(Consumer<DMKey> result) {
        return new UIEnumChoice(app, result, viewOptions, buttonText.r(), entryMode);
    }

    public static UIEnumChoice.Option makeStandardOption(DMKey val, FF0 text, @Nullable Consumer<String> edit, @Nullable SchemaPath fdb) {
        return new UIEnumChoice.Option(val.toString() + " : ", text, val, edit, fdb);
    }

    public @Nullable UIEnumChoice.Option findOption(RORIO val) {
        String v2 = ValueSyntax.encode(val);
        UIEnumChoice.Option st = null;
        if (v2 != null)
            st = lookupOptions.get(v2);
        return st;
    }

    public String viewValue(RORIO val, Prefix prefix) {
        return viewValue(val, prefix, findOption(val));
    }

    public String viewValue(RORIO val, Prefix prefix, @Nullable UIEnumChoice.Option option) {
        if (option != null) {
            // prefix disposition changes how display works
            boolean shouldPrefix = false;
            if (prefix == Prefix.Prefix)
                shouldPrefix = true;
            else if (prefix == Prefix.Default)
                shouldPrefix = displayPrefixDefault;
            // continue...
            if (!shouldPrefix)
                return option.textSuffix.r();
            return option.getTextMerged();
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

    public enum Prefix {
        Default,
        Prefix,
        NoPrefix
    }
}
