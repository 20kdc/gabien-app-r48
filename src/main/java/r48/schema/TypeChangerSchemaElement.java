/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPublicPanel;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

/**
 * Created on 12/28/16.
 */
public class TypeChangerSchemaElement extends SchemaElement {
    public SchemaElement[] targets;
    public String[] typeString;

    public TypeChangerSchemaElement(String[] types, SchemaElement[] tgt) {
        typeString = types;
        targets = tgt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO targetValue, final ISchemaHost launcher, final SchemaPath path) {
        /* IPCRESS
        // Abstract away the inner object for sanity reasons
        final UIPublicPanel innerHoldingPanel = new UIPublicPanel() {
            @Override
            public void setBounds(Rect r) {
                super.setBounds(r);
                if (allElements.size() > 0)
                    allElements.getFirst().setBounds(r);
            }
        };

        initializeHoldingPanel(innerHoldingPanel, targetValue, launcher, path);

        UIElement holder = innerHoldingPanel;
        for (int i = typeString.length - 1; i >= 0; i--) {
            final int fi = i;
            final char chr = typeString[i].charAt(0);
            final String ftp = getElementSymFromTS(typeString[i]);
            holder = new UIAppendButton(Character.toString(chr), holder, new Runnable() {
                @Override
                public void run() {
                    targetValue.setNull();
                    targetValue.type = chr;
                    targetValue.symVal = ftp;
                    targets[fi].modifyVal(targetValue, path, true);
                    path.changeOccurred(false);
                    // auto-updates
                }
            }, FontSizes.schemaButtonTextHeight);
        }
        return holder;
        */
        return HiddenSchemaElement.makeHiddenElementIpcress();
    }

    private int getRelevantElementId(RubyIO targetValue) {
        for (int i = 0; i < typeString.length; i++) {
            if (typeString[i].charAt(0) == targetValue.type) {
                if (targetValue.symVal == null)
                    if (typeString[i].length() == 1)
                        return i;
                if (targetValue.symVal.equals(typeString[i].substring(1)))
                    return i;
            }
        }
        return -1;
    }

    private String getElementSymFromTS(String outerString) {
        String tp = outerString.substring(1);
        if (tp.length() == 0)
            tp = null;
        return tp;
    }

    /*IPCRESS
    private void initializeHoldingPanel(UIPublicPanel innerHoldingPanel, RubyIO targetValue, ISchemaHost l, SchemaPath path) {
        int rei = getRelevantElementId(targetValue);
        SchemaElement targetS = new OpaqueSchemaElement();
        if (rei != -1)
            targetS = targets[rei];
        Rect b = innerHoldingPanel.getBounds();
        innerHoldingPanel.clearElements();
        UIElement uie = targetS.buildHoldingEditor(targetValue, l, path);
        uie.setBounds(new Rect(0, 0, b.width, b.height));
        innerHoldingPanel.addElement(uie);
        innerHoldingPanel.setBounds(new Rect(0, 0, b.width, b.height));
    }*/

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        int rei = getRelevantElementId(target);
        if (rei == -1)
            rei = targets.length - 1;
        SchemaElement targetS = targets[rei];

        boolean modified = false;
        int wantedType = typeString[rei].charAt(0);
        if (target.type != wantedType)
            modified = true;
        target.type = wantedType;

        String wantedSym = getElementSymFromTS(typeString[rei]);
        if ((target.symVal == null) && (wantedSym != null)) {
            target.symVal = wantedSym;
            modified = true;
        } else if (wantedSym != null) {
            if (!target.symVal.equals(wantedSym)) {
                target.symVal = wantedSym;
                modified = true;
            }
        }
        // If the target performs a correction, it will cause a changeOccurred.
        targetS.modifyVal(target, path, setDefault || modified);
        if (modified)
            path.changeOccurred(true);
    }
}
