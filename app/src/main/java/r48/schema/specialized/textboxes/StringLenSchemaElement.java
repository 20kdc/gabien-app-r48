/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.textboxes;

import gabien.ui.*;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.io.data.IRIO;
import r48.schema.StringSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.ui.UIAppendButton;

/**
 * Created on August 31st 2017.
 */
public abstract class StringLenSchemaElement extends StringSchemaElement {
    public final TextRules textRules = new R2kTextRules();

    public StringLenSchemaElement(App app, FF0 arg) {
        super(app, arg, '"');
    }

    public abstract int getLen();

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextBox utb = (UITextBox) super.buildHoldingEditorImpl(target, launcher, path);
        utb.feedback = (s) -> {
            int l1 = textRules.countCells(s);
            return Integer.toString(getLen() - l1);
        };
        UITextButton uimu = new UITextButton("-00000", app.f.schemaFieldTH, null) {
            @Override
            public void updateContents(double deltaTime, boolean selected, IPeripherals peripherals) {
                super.updateContents(deltaTime, selected, peripherals);
                int l1 = textRules.countCells(utb.getText());
                setText(Integer.toString(getLen() - l1));
            }
        };
        uimu.onClick = () -> {
            app.ui.wm.createMenu(uimu, new UITextStuffMenu(app, () -> {
                return new String[] {utb.getText()};
            }, (res) -> {
                if (res.length < 1) {
                    // refuse
                } else {
                    utb.setText(res[0]);
                    utb.onEdit.run();
                }
            }, textRules, getLen()));
        };
        return new UIAppendButton(uimu, utb);
    }
}
