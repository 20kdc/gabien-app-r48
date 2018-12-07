/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.io.data.IRIO;

import java.util.LinkedList;

/**
 * And this is why I went and abstracted array UI.
 * 25th October 2017.
 */
public class PagerArrayInterface implements IArrayInterface {
    @Override
    public void provideInterfaceFrom(UIScrollLayout svl, final ISupplier<Boolean> valid, final IFunction<String, IProperty> prop, final ISupplier<ArrayPosition[]> getPositions) {
        final ArrayPosition[] positions = getPositions.get();
        LinkedList<UIElement> uie = new LinkedList<UIElement>();
        for (int i = 0; i < positions.length; i++) {
            final String i2 = Integer.toString(i + 1);
            // "+", "+>", "-", "Cp.", "Ps."
            UIScrollLayout barLayout = new UIScrollLayout(false, FontSizes.mapToolbarScrollersize);
            if (positions[i].execInsert != null) {
                final Runnable r = positions[i].execInsert;
                barLayout.panelsAdd(new UITextButton("+", FontSizes.schemaFieldTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        r.run();
                    }
                }));
            }
            if (i < positions.length - 1) {
                if (positions[i + 1].execInsert != null) {
                    final Runnable r = positions[i + 1].execInsert;
                    barLayout.panelsAdd(new UITextButton("+>", FontSizes.schemaFieldTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    }));
                }
            }
            if (positions[i].execDelete != null) {
                final ISupplier<Runnable> r = positions[i].execDelete;
                barLayout.panelsAdd(new UITextButton("-", FontSizes.schemaFieldTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        r.get().run();
                    }
                }));
            }
            final IRIO[] copyMe = positions[i].elements;
            if (copyMe != null) {
                barLayout.panelsAdd(new UITextButton(TXDB.get("Copy"), FontSizes.schemaFieldTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        RubyIO rio = new RubyIO();
                        rio.type = '[';

                        rio.arrVal = new IRIO[copyMe.length];
                        for (int j = 0; j < copyMe.length; j++)
                            rio.arrVal[j] = new RubyIO().setDeepClone(copyMe[j]);

                        AppMain.theClipboard = rio;
                    }
                }));
            }
            if (i < positions.length - 1) {
                if (positions[i + 1].execInsertCopiedArray != null) {
                    final Runnable r = positions[i + 1].execInsertCopiedArray;
                    barLayout.panelsAdd(new UITextButton(TXDB.get("Paste"), FontSizes.schemaFieldTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    }));
                }
            }
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
        UITabPane utp = new UITabPane(FontSizes.tabTextHeight, false, false, FontSizes.schemaPagerTabScrollersize) {
            @Override
            public void selectTab(UIElement i) {
                super.selectTab(i);
                prop2.accept((double) getTabIndex());
            }
        };

        for (UIElement ue : uie)
            utp.addTab(new TabUtils.Tab(ue, new TabUtils.TabIcon[] {}));
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
