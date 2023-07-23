/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.tr.pages.TrRoot;

import java.util.LinkedList;

/**
 * And this is why I went and abstracted array UI.
 * 25th October 2017.
 */
public class PagerArrayInterface implements IArrayInterface {
    public StandardArrayInterface regularArrayInterface = new StandardArrayInterface();
    @Override
    public void provideInterfaceFrom(final Host svl, final ISupplier<Boolean> valid, final IFunction<String, IProperty> prop, final ISupplier<ArrayPosition[]> getPositions) {
        final App app = svl.getApp();
        final TrRoot T = app.t;
        // work out if we want to be in regular array mode
        final IProperty regularArrayMode = prop.apply("regularArrayMode");
        final boolean regularArrayModeCurrent = ((int) ((double) regularArrayMode.get())) != 0;
        Runnable swapModeAndReset = new Runnable() {
            @Override
            public void run() {
                if (regularArrayModeCurrent) {
                    regularArrayMode.accept(0d);
                } else {
                    regularArrayMode.accept(1d);
                }
                svl.panelsClear();
                provideInterfaceFrom(svl, valid, prop, getPositions);
            }
        };
        if (regularArrayModeCurrent) {
            // regular array mode
            final UITextButton swapModeButton = new UITextButton(T.s.l198, app.f.schemaFieldTH, swapModeAndReset);
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
            UIScrollLayout barLayout = new UIScrollLayout(false, app.f.mapToolbarS);
            if (positions[i].execInsert != null) {
                final Runnable r = positions[i].execInsert;
                barLayout.panelsAdd(new UITextButton("+", app.f.schemaFieldTH, new Runnable() {
                    @Override
                    public void run() {
                        r.run();
                    }
                }));
            }
            if (i < positions.length - 1) {
                if (positions[i + 1].execInsert != null) {
                    final Runnable r = positions[i + 1].execInsert;
                    barLayout.panelsAdd(new UITextButton("+>", app.f.schemaFieldTH, new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    }));
                }
            }
            if (positions[i].execDelete != null) {
                final ISupplier<Runnable> r = positions[i].execDelete;
                final String posText = positions[i].text;
                UITextButton button = new UITextButton("-", app.f.schemaFieldTH, null);
                button.onClick = () -> {
                    app.ui.confirmDeletion(false, posText, button, () -> r.get().run());
                };
                barLayout.panelsAdd(button);
            }
            final IRIO[] copyMe = positions[i].elements;
            if (copyMe != null) {
                barLayout.panelsAdd(new UITextButton(T.g.bCopy, app.f.schemaFieldTH, new Runnable() {
                    @Override
                    public void run() {
                        IRIOGeneric rio = new IRIOGeneric(app.encoding);
                        rio.setArray(copyMe.length);

                        for (int j = 0; j < copyMe.length; j++)
                            rio.getAElem(j).setDeepClone(copyMe[j]);

                        app.theClipboard = rio;
                    }
                }));
            }
            if (i < positions.length - 1) {
                if (positions[i + 1].execInsertCopiedArray != null) {
                    final Runnable r = positions[i + 1].execInsertCopiedArray;
                    barLayout.panelsAdd(new UITextButton(T.g.bPaste, app.f.schemaFieldTH, new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    }));
                }
            }
            barLayout.panelsAdd(new UITextButton(T.s.l199, app.f.schemaFieldTH, swapModeAndReset));
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
        final IProperty prop2 = prop.apply("page");
        final IProperty scrollProp = prop.apply("pageTabScroll");
        UITabPane utp = new UITabPane(app.f.tabTH, false, false, app.f.schemaPagerTabS) {
            @Override
            public void selectTab(UIElement i) {
                super.selectTab(i);
                prop2.accept((double) getTabIndex());
            }
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                super.update(deltaTime, selected, peripherals);
                scrollProp.accept(getScrollPoint());
            }
        };
        utp.setScrollPoint(scrollProp.get());

        for (UIElement ue : uie)
            utp.addTab(new UITabBar.Tab(ue, new UITabBar.TabIcon[] {}));
        svl.panelsAdd(utp);
        int state = (int) ((double) prop2.get());
        if (state < 0)
            state = 0;
        if (state >= uie.size())
            state = uie.size() - 1;
        if (uie.size() > 0)
            utp.selectTab(uie.get(state));
    }
}
