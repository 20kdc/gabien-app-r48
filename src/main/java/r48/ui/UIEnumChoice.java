/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.ui.*;
import r48.FontSizes;
import r48.UITest;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Used for RPG Command Selection.
 * Created on 12/30/16.
 */
public class UIEnumChoice extends UIPanel implements IWindowElement {
    UIScrollVertLayout uiSVL = new UIScrollVertLayout();
    UIHHalfsplit finalSplit;
    UINumberBox nb;
    boolean wantsSelfClose = false;

    public UIEnumChoice(final IConsumer<Integer> result, final HashMap<String, Integer> options, String buttonText) {
        this(result, options, UITest.sortedKeysStr(options.keySet()), buttonText);
    }

    public UIEnumChoice(final IConsumer<Integer> result, final HashMap<String, Integer> options, final LinkedList<String> order, String buttonText) {
        for (String key : order) {
            final int r = options.get(key);
            uiSVL.panels.add(new UITextButton(FontSizes.enumChoiceTextHeight, key, new Runnable() {
                @Override
                public void run() {
                    if (!wantsSelfClose)
                        result.accept(r);
                    wantsSelfClose = true;
                }
            }));
        }
        nb = new UINumberBox(FontSizes.schemaFieldTextHeight);
        finalSplit = new UIHHalfsplit(1, 3, nb, new UITextButton(FontSizes.schemaButtonTextHeight, buttonText, new Runnable() {
            @Override
            public void run() {
                if (!wantsSelfClose)
                    result.accept(nb.number);
                wantsSelfClose = true;
            }
        }));
        if (buttonText.length() != 0)
            uiSVL.panels.add(finalSplit);
        allElements.add(uiSVL);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        uiSVL.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public boolean wantsSelfClose() {
        return wantsSelfClose;
    }

    @Override
    public void windowClosed() {

    }
}
