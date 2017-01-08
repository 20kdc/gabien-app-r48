/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema;

import gabien.ui.*;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.RubyIO;
import gabienapp.schema.util.SchemaPath;
import gabienapp.ui.UIAppendButton;

/**
 * Created on 12/28/16.
 */
public class TypeChangerSchemaElement implements ISchemaElement {
    public ISchemaElement[] targets;
    public String[] typeString;

    public TypeChangerSchemaElement(String[] types, ISchemaElement[] tgt) {
        typeString = types;
        targets = tgt;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO targetValue, final ISchemaHost launcher, final SchemaPath path) {
        // Abstract away the inner object for sanity reasons
        final UIPanel innerHoldingPanel = new UIPanel() {
            @Override
            public void setBounds(Rect r) {
                super.setBounds(r);
                if (allElements.size() > 0)
                    allElements.getFirst().setBounds(r);
            }
        };
        innerHoldingPanel.setBounds(new Rect(0, 0, 128, maxHoldingHeight()));

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
                    initializeHoldingPanel(innerHoldingPanel, targetValue, launcher, path);
                }
            }, false);
        }
        return holder;
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

    private void initializeHoldingPanel(UIPanel innerHoldingPanel, RubyIO targetValue, ISchemaHost l, SchemaPath path) {
        int rei = getRelevantElementId(targetValue);
        ISchemaElement targetS = new OpaqueSchemaElement();
        if (rei != -1)
            targetS = targets[rei];
        Rect b = innerHoldingPanel.getBounds();
        innerHoldingPanel.allElements.clear();
        UIElement uie = targetS.buildHoldingEditor(targetValue, l, path);
        uie.setBounds(new Rect(0, 0, b.width, b.height));
        innerHoldingPanel.allElements.add(uie);
    }

    @Override
    public int maxHoldingHeight() {
        int holdHeight = 10;
        for (ISchemaElement ise : targets) {
            int nextHeight = ise.maxHoldingHeight();
            if (holdHeight < nextHeight)
                holdHeight = nextHeight;
        }
        return holdHeight;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        int rei = getRelevantElementId(target);
        if (rei == -1)
            rei = targets.length - 1;
        ISchemaElement targetS = targets[rei];

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
