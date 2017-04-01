/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIEnumChoice;

import java.util.HashMap;

/**
 * Enum. Note that it is critical to implementation of many things that this explicitly switch into a new view.
 * Specifically, the switch will cause a UI rebuild upon return, which is required to keep data consistency.
 * Created on 12/30/16.
 */
public class EnumSchemaElement implements ISchemaElement {
    public HashMap<Integer, String> options;
    public HashMap<String, Integer> viewOptions;
    public String buttonText;

    public EnumSchemaElement(HashMap<Integer, String> o, String bt) {
        options = o;
        viewOptions = new HashMap<String, Integer>();
        for (Integer si : options.keySet())
            viewOptions.put(viewValue(si, true), si);
        buttonText = bt;
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
                }, viewOptions, buttonText), path), target, launcher));
            }
        });
    }

    public String viewValue(int fixnumVal, boolean prefix) {
        String st = options.get(fixnumVal);
        if (st == null)
            return "int:" + fixnumVal;
        if (!prefix)
            return st;
        return fixnumVal + ":" + st;
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, 'i', setDefault)) {
            target.fixnumVal = options.keySet().iterator().next();
            path.changeOccurred(true);
        }
    }
}
