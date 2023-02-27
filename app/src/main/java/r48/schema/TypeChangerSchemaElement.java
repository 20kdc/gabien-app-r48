/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.App;
import r48.FontSizes;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * Created on 12/28/16.
 */
public class TypeChangerSchemaElement extends SchemaElement {
    public SchemaElement[] targets;
    public String[] typeString;

    public TypeChangerSchemaElement(App app, String[] types, SchemaElement[] tgt) {
        super(app);
        typeString = types;
        targets = tgt;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO targetValue, final ISchemaHost launcher, final SchemaPath path) {
        int rei = getRelevantElementId(targetValue);

        SchemaElement targetS = new OpaqueSchemaElement(app);
        if (rei != -1)
            targetS = targets[rei];

        UIElement holder = targetS.buildHoldingEditor(targetValue, launcher, path);

        for (int i = typeString.length - 1; i >= 0; i--) {
            final int fi = i;
            final char chr = typeString[i].charAt(0);
            holder = new UIAppendButton(Character.toString(chr), holder, new Runnable() {
                @Override
                public void run() {
                    targetValue.setNull();
                    targets[fi].modifyVal(targetValue, path, true);
                    path.changeOccurred(false);
                    // auto-updates
                }
            }, FontSizes.schemaFieldTextHeight);
        }
        return holder;
    }

    private int getRelevantElementId(IRIO targetValue) {
        int type = targetValue.getType();
        for (int i = 0; i < typeString.length; i++) {
            if (typeString[i].charAt(0) == type) {
                if (typeString[i].length() != 1) {
                    if (targetValue.getSymbol().equals(typeString[i].substring(1)))
                        return i;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        int rei = -1;
        if (!setDefault)
            rei = getRelevantElementId(target);
        if (rei == -1) {
            rei = targets.length - 1;
            setDefault = true;
        }

        SchemaElement targetS = targets[rei];

        // If the target performs a correction, it will cause a changeOccurred.
        targetS.modifyVal(target, path, setDefault);
    }
}
