/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.dbs.FormatSyntax;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.ISpritesheetProvider;
import r48.ui.dialog.UISpritesheetChoice;

/**
 * Created on 29/07/17.
 */
public class SpritesheetCoreSchemaElement extends SchemaElement {
    public String text;
    public int defaultVal;

    public IFunction<IRIO, IRIO> numberProvider;
    public IFunction<IRIO, ISpritesheetProvider> provider;

    public SpritesheetCoreSchemaElement(String propTranslated, int def, IFunction<IRIO, IRIO> nprov, IFunction<IRIO, ISpritesheetProvider> core) {
        text = propTranslated;
        defaultVal = def;
        numberProvider = nprov;
        provider = core;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final ISpritesheetProvider localProvider = provider.apply(target);
        final IRIO actTarg = numberProvider.apply(target);
        return new UITextButton(FormatSyntax.formatExtended(text, actTarg), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                TempDialogSchemaChoice temp = new TempDialogSchemaChoice(null, null, path);
                final SchemaPath innerPath = path.newWindow(temp, target);
                temp.heldDialog = new UISpritesheetChoice(actTarg.getFX(), localProvider, new IConsumer<Long>() {
                    @Override
                    public void accept(Long integer) {
                        actTarg.setFX(integer);
                        innerPath.changeOccurred(false);
                        launcher.popObject();
                    }
                });
                launcher.pushObject(innerPath);
            }
        });
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        IRIO actTarg = numberProvider.apply(target);
        if (checkType(actTarg, 'i', null, setDefault)) {
            actTarg.setFX(defaultVal);
            path.changeOccurred(true);
        }
    }
}
