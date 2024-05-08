/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UITabPane;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.EmbedDataSlot;
import r48.schema.util.IEmbedDataContext;
import r48.tr.pages.TrRoot;

import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * And this is why I went and abstracted array UI.
 * 25th October 2017.
 */
public class PagerArrayInterface implements IArrayInterface {
    public StandardArrayInterface regularArrayInterface = new StandardArrayInterface();
    public final EmbedDataKey<Boolean> regularArrayModeKey = new EmbedDataKey<>();
    public final EmbedDataKey<Integer> pageKey = new EmbedDataKey<>();
    public final EmbedDataKey<Double> pageTabScrollKey = new EmbedDataKey<>();
    @Override
    public void provideInterfaceFrom(final Host svl, final Supplier<Boolean> valid, final IEmbedDataContext prop, final Supplier<ArrayPosition[]> getPositions) {
        final App app = svl.getApp();
        final TrRoot T = app.t;
        // work out if we want to be in regular array mode
        final EmbedDataSlot<Boolean> regularArrayMode = prop.embedSlot(regularArrayModeKey, false);
        final boolean regularArrayModeCurrent = regularArrayMode.value;
        Runnable swapModeAndReset = () -> {
            regularArrayMode.value = !regularArrayModeCurrent;
            svl.panelsClear();
            provideInterfaceFrom(svl, valid, prop, getPositions);
        };
        if (regularArrayModeCurrent) {
            // regular array mode
            final UITextButton swapModeButton = new UITextButton(T.s.array_bModeRegular, app.f.schemaFieldTH, swapModeAndReset);
            svl.panelsAdd(swapModeButton);
            regularArrayInterface.provideInterfaceFrom(new Host() {

                @Override
                public void panelsClear() {
                    svl.panelsClear();
                    svl.panelsAdd(swapModeButton);
                }

                @Override
                public void panelsAdd(UIElement element) {
                    svl.panelsAdd(element);
                }

                @Override
                public void panelsFinished() {
                    svl.panelsFinished();
                }

                @Override
                public App getApp() {
                    return app;
                }
            }, valid, prop, getPositions);
            return;
        }

        final ArrayPosition[] positions = getPositions.get();
        LinkedList<UIElement> uie = new LinkedList<UIElement>();
        for (int i = 0; i < positions.length; i++) {
            final String i2 = Integer.toString(i + 1);
            // "+", "+>", "-", "Cp.", "Ps."
            LinkedList<UIElement> barLayoutList = new LinkedList<>();
            if (positions[i].execInsert != null) {
                final Runnable r = positions[i].execInsert;
                barLayoutList.add(new UITextButton("+", app.f.schemaFieldTH, () -> {
                    r.run();
                }));
            }
            if (i < positions.length - 1) {
                if (positions[i + 1].execInsert != null) {
                    final Runnable r = positions[i + 1].execInsert;
                    barLayoutList.add(new UITextButton("+>", app.f.schemaFieldTH, () -> {
                        r.run();
                    }));
                }
            }
            if (positions[i].execDelete != null) {
                final Supplier<Runnable> r = positions[i].execDelete;
                final String posText = positions[i].text;
                UITextButton button = new UITextButton("-", app.f.schemaFieldTH, null);
                button.onClick = () -> {
                    app.ui.confirmDeletion(false, posText, button, () -> r.get().run());
                };
                barLayoutList.add(button);
            }
            final IRIO[] copyMe = positions[i].elements;
            if (copyMe != null) {
                barLayoutList.add(new UITextButton(T.g.bCopy, app.f.schemaFieldTH, () -> {
                    IRIOGeneric rio = new IRIOGeneric(app.ctxClipboardAppEncoding);
                    rio.setArray(copyMe.length);

                    for (int j = 0; j < copyMe.length; j++)
                        rio.getAElem(j).setDeepClone(copyMe[j]);

                    app.theClipboard = rio;
                }));
            }
            if (i < positions.length - 1) {
                if (positions[i + 1].execInsertCopiedArray != null) {
                    final Runnable r = positions[i + 1].execInsertCopiedArray;
                    barLayoutList.add(new UITextButton(T.g.bPaste, app.f.schemaFieldTH, () -> {
                        r.run();
                    }));
                }
            }
            barLayoutList.add(new UITextButton(T.s.array_bModePager, app.f.schemaFieldTH, swapModeAndReset));
            UIScrollLayout barLayout = new UIScrollLayout(false, app.f.mapToolbarS);
            barLayout.panelsSet(barLayoutList);
            if (positions[i].core != null) {
                uie.add(new UISplitterLayout(barLayout, positions[i].core, true, 0d) {
                    @Override
                    public String toString() {
                        return i2;
                    }
                });
            } else if (positions.length == 1) {
                // If there's only one position here, then it's this one, and this entry is needed for the +>
                // Thus, return with just this
                svl.panelsAdd(barLayout);
                return;
            }
        }
        final EmbedDataSlot<Integer> prop2 = prop.embedSlot(pageKey, 0);
        final EmbedDataSlot<Double> scrollProp = prop.embedSlot(pageTabScrollKey, 0.0d);
        UITabPane utp = new UITabPane(app.f.tabTH, false, false, app.f.schemaPagerTabS) {
            @Override
            public void selectTab(UIElement i) {
                super.selectTab(i);
                prop2.value = getTabIndex();
            }
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                super.update(deltaTime, selected, peripherals);
                scrollProp.value = getScrollPoint();
            }
        };
        utp.setScrollPoint(scrollProp.value);

        for (UIElement ue : uie)
            utp.addTab(new UITabBar.Tab(ue, new UITabBar.TabIcon[] {}));
        svl.panelsAdd(utp);
        int state = prop2.value;
        if (state < 0)
            state = 0;
        if (state >= uie.size())
            state = uie.size() - 1;
        if (uie.size() > 0)
            utp.selectTab(uie.get(state));
        svl.panelsFinished();
    }
}
