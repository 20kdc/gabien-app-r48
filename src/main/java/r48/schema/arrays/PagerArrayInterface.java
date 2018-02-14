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
import r48.ui.Art;
import r48.ui.UISymbolButton;

import java.util.LinkedList;

/**
 * And this is why I went and abstracted array UI.
 * 25th October 2017.
 */
public class PagerArrayInterface implements IArrayInterface {
    @Override
    public void provideInterfaceFrom(UIScrollLayout svl, final IFunction<String, IProperty> prop, final ISupplier<ArrayPosition[]> getPositions) {
        final ArrayPosition[] positions = getPositions.get();
        LinkedList<UIElement> uie = new LinkedList<UIElement>();
        for (int i = 0; i < positions.length; i++) {
            final String i2 = Integer.toString(i + 1);
            if (positions[i].core != null) {
                // "+", "+>", "-", "Cp.", "Ps."
                UIScrollLayout barLayout = new UIScrollLayout(false, FontSizes.mapToolbarScrollersize);
                if (positions[i].execInsert != null) {
                    final Runnable r = positions[i].execInsert;
                    barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, "+", new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    }));
                }
                if (i < positions.length - 1) {
                    if (positions[i + 1].execInsert != null) {
                        final Runnable r = positions[i + 1].execInsert;
                        barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, "+>", new Runnable() {
                            @Override
                            public void run() {
                                r.run();
                            }
                        }));
                    }
                }
                if (positions[i].execDelete != null) {
                    final ISupplier<Runnable> r = positions[i].execDelete;
                    barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, "-", new Runnable() {
                        @Override
                        public void run() {
                            r.get().run();
                        }
                    }));
                }
                final RubyIO[] copyMe = positions[i].elements;
                barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Copy"), new Runnable() {
                    @Override
                    public void run() {
                        RubyIO rio = new RubyIO();
                        rio.type = '[';

                        rio.arrVal = new RubyIO[copyMe.length];
                        for (int j = 0; j < copyMe.length; j++)
                            rio.arrVal[j] = new RubyIO().setDeepClone(copyMe[j]);

                        AppMain.theClipboard = rio;
                    }
                }));
                if (i < positions.length - 1) {
                    if (positions[i + 1].execInsertCopiedArray != null) {
                        final Runnable r = positions[i + 1].execInsertCopiedArray;
                        barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Paste"), new Runnable() {
                            @Override
                            public void run() {
                                r.run();
                            }
                        }));
                    }
                }
                barLayout.setBounds(new Rect(0, 0, 128, UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height + FontSizes.mapToolbarScrollersize));
                uie.add(new UISplitterLayout(barLayout, positions[i].core, true, 0d) {
                    @Override
                    public String toString() {
                        return i2;
                    }
                });
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

        int h = utp.tabBarHeight + FontSizes.schemaPagerTabScrollersize;
        for (UIElement ue : uie) {
            utp.addTab(new UIWindowView.WVWindow(ue, new UIWindowView.IWVWindowIcon[] {}));
            h = Math.max(h, ue.getBounds().height + utp.tabBarHeight + FontSizes.schemaPagerTabScrollersize);
        }
        utp.setBounds(new Rect(0, 0, 200, h));
        svl.panels.add(utp);
        int state = (int) ((double) prop2.get());
        if (state < 0)
            state = 0;
        if (state >= uie.size())
            state = uie.size() - 1;
        if (uie.size() > 0)
            utp.selectTab(uie.get(state));
    }
}
