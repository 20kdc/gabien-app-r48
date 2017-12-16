/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.Application;
import r48.FontSizes;
import r48.dbs.TXDB;

import java.util.LinkedList;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIPanel {
    private UIScrollLayout outerLayout;
    private int lastFontSizerSize = -1;

    public UIFontSizeConfigurator() {
        refreshLayout();
        setBounds(new Rect(0, 0, FontSizes.scaleGuess(320), FontSizes.scaleGuess(240)));
    }

    public void refreshLayout() {
        double iniScroll = 0;
        if (outerLayout != null)
            iniScroll = outerLayout.scrollbar.scrollPoint;
        if (lastFontSizerSize == FontSizes.fontSizerTextHeight)
            return;
        lastFontSizerSize = FontSizes.fontSizerTextHeight;
        allElements.clear();
        outerLayout = new UIScrollLayout(true, FontSizes.generalScrollersize);
        outerLayout.scrollbar.scrollPoint = iniScroll;
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
        UITextButton fontButton = new UITextButton(FontSizes.fontSizerTextHeight, "", new Runnable() {
            @Override
            public void run() {
                if (UILabel.fontOverride != null) {
                    UILabel.fontOverride = null;
                } else {
                    Application.preventFontOverrider = false;
                    UILabel.fontOverride = GaBIEn.getFontOverrides()[0];
                }
            }
        }) {
            @Override
            public void updateAndRender(int ox, int oy, double DeltaTime, boolean selected, IGrInDriver igd) {
                text = TXDB.get("Font: ");
                if (UILabel.fontOverride != null) {
                    text += UILabel.fontOverride;
                } else {
                    text += TXDB.get("Internal w/fallbacks");
                }
                super.updateAndRender(ox, oy, DeltaTime, selected, igd);
            }
        };
        final UIAppendButton fontButtonAppend = new UIAppendButton(TXDB.get("Even for height <= 8"), fontButton, new Runnable() {
            @Override
            public void run() {
            }
        }, FontSizes.fontSizerTextHeight);
        fontButtonAppend.button.togglable().state = UILabel.fontOverrideUE8;
        fontButtonAppend.button.onClick = new Runnable() {
            @Override
            public void run() {
                UILabel.fontOverrideUE8 = fontButtonAppend.button.state;
            }
        };
        outerLayout.panels.add(fontButtonAppend);
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
