/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
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

    public IFunction<RubyIO, RubyIO> numberProvider;
    public IFunction<RubyIO, ISpritesheetProvider> provider;

    public SpritesheetCoreSchemaElement(String propTranslated, int def, IFunction<RubyIO, RubyIO> nprov, IFunction<RubyIO, ISpritesheetProvider> core) {
        text = propTranslated;
        defaultVal = def;
        numberProvider = nprov;
        provider = core;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final ISpritesheetProvider localProvider = provider.apply(target);
        final RubyIO actTarg = numberProvider.apply(target);
        return new UITextButton(FormatSyntax.formatExtended(text, actTarg), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                TempDialogSchemaChoice temp = new TempDialogSchemaChoice(null, null, path);
                final SchemaPath innerPath = path.newWindow(temp, target);
                temp.heldDialog = new UISpritesheetChoice(actTarg.fixnumVal, localProvider, new IConsumer<Long>() {
                    @Override
                    public void accept(Long integer) {
                        actTarg.fixnumVal = integer;
                        innerPath.changeOccurred(false);
                        launcher.popObject();
                    }
                });
                launcher.pushObject(innerPath);
            }
        });
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        RubyIO actTarg = numberProvider.apply(target);
        if (SchemaElement.ensureType(actTarg, 'i', setDefault)) {
            actTarg.fixnumVal = defaultVal;
            path.changeOccurred(true);
        }
    }
}
