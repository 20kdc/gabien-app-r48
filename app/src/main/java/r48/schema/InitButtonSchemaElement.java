/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;

/**
 * The most horrifying creation I have ever wrought.
 * This button is essentially the logical endpoint of LengthChangeSchemaElement.
 * To explain what's going on with the two 'default' flags:
 * runDef means "run this as default".
 * "def" means "include the interior when setDefault is true in modifyVal.".
 * When being used as part of the LengthChangeSchemaElement emulation,
 * we want to preserve the old values, so runDef is false.
 * When being used as an initializer button, the old values aren't needed,
 * so runDef is true.
 * Created on April 20, 2019.
 */
public class InitButtonSchemaElement extends SchemaElement.Leaf {
    private final FF0 text;
    private final Condition condition;
    private final SchemaElement reinitializer;
    private final boolean defaulting, asDefault;

    public InitButtonSchemaElement(@NonNull FF0 text2, @NonNull Condition cond, @NonNull SchemaElement reinit, boolean runDef, boolean def) {
        super(reinit.app);
        text = text2;
        condition = cond;
        reinitializer = reinit;
        defaulting = def;
        asDefault = runDef;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        UITextButton utb = new UITextButton(text.r(), app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                // This is going to show up as a modifyVal changeOccurred, so it needs to be run again
                //  to trigger the subwatcher stuff
                reinitializer.modifyVal(target, path, asDefault);
                path.changeOccurred(false);
            }
        }).togglable(condition.eval(target));
        return utb;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // No need to perform a second changeOccurred
        if (defaulting && setDefault)
            reinitializer.modifyVal(target, path, true);
    }

    public interface Condition {
        boolean eval(RORIO target);
    }
}
