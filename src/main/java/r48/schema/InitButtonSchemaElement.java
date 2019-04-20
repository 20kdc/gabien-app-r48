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
import r48.dbs.FormatSyntax;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

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
public class InitButtonSchemaElement extends SchemaElement {
    private final String text, condition;
    private final SchemaElement reinitializer;
    private final boolean defaulting, asDefault;

    public InitButtonSchemaElement(String text2, String cond, SchemaElement reinit, boolean runDef, boolean def) {
        text = text2;
        condition = cond;
        reinitializer = reinit;
        defaulting = def;
        asDefault = runDef;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        UITextButton utb = new UITextButton(text, FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                // This is going to show up as a modifyVal changeOccurred, so it needs to be run again
                //  to trigger the subwatcher stuff
                reinitializer.modifyVal(target, path, asDefault);
                path.changeOccurred(false);
            }
        }).togglable(evaluateCondition(target));
        return utb;
    }

    private boolean evaluateCondition(IRIO target) {
        return !FormatSyntax.formatNameExtended(condition, target, new IRIO[0], null).equals("0");
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // No need to perform a second changeOccurred
        if (defaulting && setDefault)
            reinitializer.modifyVal(target, path, true);
    }
}
