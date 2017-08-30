/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIPanel {
    private UIScrollLayout outerLayout;
    private int lastFontSizerSize = -1;

    public UIFontSizeConfigurator() {
        refreshLayout();
        setBounds(new Rect(0, 0, 320, 240));
    }

    public void refreshLayout() {
        if (lastFontSizerSize == FontSizes.fontSizerTextHeight)
            return;
        lastFontSizerSize = FontSizes.fontSizerTextHeight;
        allElements.clear();
        outerLayout = new UIScrollLayout(true, FontSizes.generalScrollersize);
        final LinkedList<Runnable> doubleAll = new LinkedList<Runnable>();
        final LinkedList<Runnable> halfAll = new LinkedList<Runnable>();
        outerLayout.panels.add(new UISplitterLayout(new UITextButton(FontSizes.fontSizerTextHeight, "*2", new Runnable() {
            @Override
            public void run() {
                for (Runnable r : doubleAll)
                    r.run();
                refreshLayout();
            }
        }), new UITextButton(FontSizes.fontSizerTextHeight, "/2", new Runnable() {
            @Override
            public void run() {
                for (Runnable r : halfAll)
                    r.run();
                refreshLayout();
            }
        }), false, 1, 2));
        outerLayout.panels.add(new UISplitterLayout(new UITextButton(FontSizes.fontSizerTextHeight, TXDB.get("Save"), new Runnable() {
            @Override
            public void run() {
                FontSizes.save();
            }
        }), new UITextButton(FontSizes.fontSizerTextHeight, TXDB.get("Load"), new Runnable() {
            @Override
            public void run() {
                FontSizes.load();
                refreshLayout();
            }
        }), false, 1, 2));
        outerLayout.panels.add(new UITextButton(FontSizes.fontSizerTextHeight, "", new Runnable() {
            @Override
            public void run() {
                if (UILabel.fontOverride != null) {
                    UILabel.fontOverride = null;
                } else {
                    UILabel.fontOverride = GaBIEn.getFontOverrides()[0];
                }
            }
        }) {
            @Override
            public void updateAndRender(int ox, int oy, double DeltaTime, boolean selected, IGrInDriver igd) {
                if (UILabel.fontOverride != null) {
                    Text = TXDB.get("Font: " + UILabel.fontOverride);
                } else {
                    Text = TXDB.get("Font: Internal w/fallbacks");
                }
                super.updateAndRender(ox, oy, DeltaTime, selected, igd);
            }
        });
        try {
            for (final FontSizes.FontSizeField field : FontSizes.getFields()) {
                    doubleAll.add(new Runnable() {
                        @Override
                        public void run() {
                            field.accept(field.get() * 2);
                        }
                    });
                    halfAll.add(new Runnable() {
                        @Override
                        public void run() {
                            field.accept(field.get() / 2);
                        }
                    });
                    UIAdjuster tb = new UIAdjuster(FontSizes.fontSizerTextHeight, new ISupplier<String>() {
                        @Override
                        public String get() {
                            int nv = field.get() + 1;
                            field.accept(nv);
                            refreshLayout();
                            return Integer.toString(nv);
                        }
                    }, new ISupplier<String>() {
                        @Override
                        public String get() {
                            int nv = field.get() - 1;
                            if (nv < 1)
                                nv = 1;
                            field.accept(nv);
                            refreshLayout();
                            return Integer.toString(nv);
                        }
                    });
                    tb.accept(Integer.toString(field.get()));
                    // NOTE: This is correct behavior due to an 'agreement' in FontSizes that this should be correct
                    outerLayout.panels.add(new UISplitterLayout(new UILabel(TXDB.get("r48", field.name), FontSizes.fontSizerTextHeight), tb, false, 4, 5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        allElements.add(outerLayout);
        Rect r = getBounds();
        outerLayout.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        outerLayout.setBounds(new Rect(0, 0, r.width, r.height));
    }
}
