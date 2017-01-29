/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;

import java.lang.reflect.Field;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIPanel {
    private UIScrollVertLayout outerLayout;
    public UIFontSizeConfigurator() {
        outerLayout = new UIScrollVertLayout();
        try {
            for (final Field field : FontSizes.class.getFields()) {
                if (field.getType() == int.class) {
                    UIAdjuster tb = new UIAdjuster(false, new ISupplier<String>() {
                        @Override
                        public String get() {
                            try {
                                int nv = field.getInt(null) + 1;
                                field.setInt(null, nv);
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
        setBounds(new Rect(0, 0, 320, 200));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        outerLayout.setBounds(new Rect(0, 0, r.width, r.height));
    }
}
