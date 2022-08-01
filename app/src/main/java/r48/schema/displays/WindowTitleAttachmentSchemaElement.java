/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.dbs.FormatSyntax;
import r48.io.data.IRIO;
import r48.schema.HiddenSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 1st August 2022.
 */
public class WindowTitleAttachmentSchemaElement extends SchemaElement {
    public final String suffixRoutine;

    public WindowTitleAttachmentSchemaElement(String sr) {
        suffixRoutine = sr;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return HiddenSchemaElement.makeHiddenElement();
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
    }

    @Override
    public @Nullable String windowTitleSuffix(SchemaPath path) {
        return FormatSyntax.formatNameExtended(suffixRoutine, path.targetElement, new IRIO[] {path.lastArrayIndex}, null);
    }
}
