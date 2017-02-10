/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIPanel {
    private UIScrollVertLayout outerLayout;
    private int lastFontSizerSize = -1;
    public UIFontSizeConfigurator() {
        refreshLayout();
        setBounds(new Rect(0, 0, 320, 200));
    }

    public void refreshLayout() {
        if (lastFontSizerSize == FontSizes.fontSizerTextHeight)
            return;
        lastFontSizerSize = FontSizes.fontSizerTextHeight;
        allElements.clear();
        outerLayout = new UIScrollVertLayout();
        final LinkedList<Runnable> doubleAll = new LinkedList<Runnable>();
        final LinkedList<Runnable> halfAll = new LinkedList<Runnable>();
        outerLayout.panels.add(new UIHHalfsplit(1, 2, new UITextButton(FontSizes.fontSizerTextHeight, "*2", new Runnable() {
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
        })));
        try {
            for (final Field field : FontSizes.class.getFields()) {
                if (field.getType() == int.class) {
                    doubleAll.add(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                field.setInt(null, field.getInt(null) * 2);
                            } catch (Exception e) {}
                        }
                    });
                    halfAll.add(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                field.setInt(null, field.getInt(null) / 2);
                            } catch (Exception e) {}
                        }
                    });
                    UIAdjuster tb = new UIAdjuster(FontSizes.fontSizerTextHeight, new ISupplier<String>() {
                        @Override
                        public String get() {
                            try {
                                int nv = field.getInt(null) + 1;
                                field.setInt(null, nv);
                                refreshLayout();
                                return Integer.toString(nv);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "ERR";
                            }
                        }
                    }, new ISupplier<String>() {
                        @Override
                        public String get() {
                            try {
                                int nv = field.getInt(null) - 1;
                                if (nv < 1)
                                    nv = 1;
                                field.setInt(null, nv);
                                refreshLayout();
                                return Integer.toString(nv);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "ERR";
                            }
                        }
                    });
                    tb.accept(Integer.toString(field.getInt(null)));
                    outerLayout.panels.add(new UIHHalfsplit(4, 5, new UILabel(field.getName(), FontSizes.fontSizerTextHeight), tb));
                }
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
