package r48.schema.arrays;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;

import java.util.LinkedList;

/**
 * And this is why I went and abstracted array UI.
 * 25th October 2017.
 */
public class PagerArrayInterface implements IArrayInterface {
    @Override
    public void provideInterfaceFrom(UIScrollLayout svl, final IProperty prop, final ArrayPosition[] positions) {
        LinkedList<UIElement> uie = new LinkedList<UIElement>();
        for (int i = 0; i < positions.length; i++) {
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
                    final Runnable r = positions[i].execDelete;
                    barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, "-", new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    }));
                }
                final RubyIO[] copyMe = positions[i].elements;
                barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Cp."), new Runnable() {
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
                        barLayout.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Ps."), new Runnable() {
                            @Override
                            public void run() {
                                r.run();
                            }
                        }));
                    }
                }
                if (barLayout.panels.size() > 0) {
                    barLayout.setBounds(new Rect(0, 0, 128, UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height + FontSizes.mapToolbarScrollersize));
                    uie.add(new UISplitterLayout(barLayout, positions[i].core, true, 0d));
                } else {
                    uie.add(positions[i].core);
                }
            }
        }
        String[] str = new String[uie.size()];
        for (int i = 0; i < str.length; i++)
            str[i] = Integer.toString(i + 1);
        UITabPane utp = new UITabPane(str, uie.toArray(new UIElement[0]), FontSizes.tabTextHeight) {
            @Override
            public void selectTab(int i) {
                super.selectTab(i);
                prop.accept((double) i);
            }
        };
        int h = utp.tabBarHeight;
        for (UIElement ue : utp.tabElems)
            h = Math.max(h, ue.getBounds().height + utp.tabBarHeight);
        utp.setBounds(new Rect(0, 0, 200, h));
        svl.panels.add(utp);
        int state = (int) ((double) prop.get());
        if (state < 0)
            state = 0;
        if (state >= utp.tabElems.length)
            state = utp.tabElems.length - 1;
        utp.selectTab(state);
    }
}
