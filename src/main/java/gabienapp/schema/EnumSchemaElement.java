/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.RubyIO;
import gabienapp.schema.util.SchemaPath;
import gabienapp.schema.specialized.TempDialogSchemaChoice;
import gabienapp.ui.UIEnumChoice;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum. Note that it is critical to implementation of many things that this explicitly switch into a new view.
 * Specifically, the switch will cause a UI rebuild upon return, which is required to keep data consistency.
 * Created on 12/30/16.
 */
public class EnumSchemaElement implements ISchemaElement {
    public HashMap<String, Integer> options;
    public String buttonText;
    public EnumSchemaElement(HashMap<String, Integer> o, String bt) {
        options = o;
        buttonText = bt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(false, viewValue((int) target.fixnumVal), new Runnable() {
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
                }, options, buttonText), path), target, launcher));
            }
        });
    }

    public String viewValue(int fixnumVal) {
        for (Map.Entry<String, Integer> e : options.entrySet()) {
            if (e.getValue() == fixnumVal)
                return e.getKey();
        }
        return "int: " + fixnumVal;
    }

    @Override
    public int maxHoldingHeight() {
        return 10;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, 'i', setDefault)) {
            target.fixnumVal = options.values().iterator().next();
            path.changeOccurred(true);
        }
    }
}
