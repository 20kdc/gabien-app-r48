/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.FontManager;
import gabien.GaBIEn;
import gabien.ui.*;
import gabienapp.Application;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;

import java.util.LinkedList;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIElement.UIProxy {
    private final UIScrollLayout outerLayout;
    private int lastFontSizerSize = -1;

    public UIFontSizeConfigurator() {
        outerLayout = new UIScrollLayout(true, FontSizes.generalScrollersize);
        refreshLayout(true);
        proxySetElement(outerLayout, false);
        setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(320), FontSizes.scaleGuess(240)));
    }

    public void refreshLayout(boolean force) {
        double iniScroll = 0;
        if (outerLayout != null)
            iniScroll = outerLayout.scrollbar.scrollPoint;
        if (!force)
            if (lastFontSizerSize == FontSizes.fontSizerTextHeight)
                return;
        lastFontSizerSize = FontSizes.fontSizerTextHeight;
        outerLayout.panelsClear();
        outerLayout.scrollbar.scrollPoint = iniScroll;
        final LinkedList<Runnable> doubleAll = new LinkedList<Runnable>();
        final LinkedList<Runnable> halfAll = new LinkedList<Runnable>();
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton("*2", FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                for (Runnable r : doubleAll)
                    r.run();
                refreshLayout(false);
            }
        }), new UITextButton("/2", FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                for (Runnable r : halfAll)
                    r.run();
                refreshLayout(false);
            }
        }), false, 1, 2));
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton(TXDB.get("Save"), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                FontSizes.save();
            }
        }), new UITextButton(TXDB.get("Load"), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                FontSizes.load(false);
                refreshLayout(true);
            }
        }), false, 1, 2));
        UITextButton fontButton = new UITextButton("", FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                if (FontManager.fontOverride != null) {
                    FontManager.fontOverride = null;
                } else {
                    FontManager.fontOverride = GaBIEn.getFontOverrides()[0];
                }
            }
        }) {
            @Override
            public void update(double dt) {
                text = TXDB.get("Font: ");
                if (FontManager.fontOverride != null) {
                    text += FontManager.fontOverride;
                } else {
                    text += TXDB.get("Internal w/fallbacks");
                }
                super.update(dt);
            }
        };
        final UIAppendButton fontButtonAppend = new UIAppendButton(TXDB.get("Even for height <= 8"), fontButton, new Runnable() {
            @Override
            public void run() {
            }
        }, FontSizes.fontSizerTextHeight);
        fontButtonAppend.button.toggle = true;
        fontButtonAppend.button.state = FontManager.fontOverrideUE8;
        fontButtonAppend.button.onClick = new Runnable() {
            @Override
            public void run() {
                FontManager.fontOverrideUE8 = fontButtonAppend.button.state;
            }
        };
        outerLayout.panelsAdd(fontButtonAppend);
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton(FormatSyntax.formatExtended(TXDB.get("Theme: #A"), new RubyIO().setFX(UIBorderedElement.borderTheme)), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                UIBorderedElement.borderTheme++;
                UIBorderedElement.borderTheme %= UIBorderedElement.BORDER_THEMES;
                refreshLayout(true);
            }
        }), new UITextButton(TXDB.get("Enable Blending"), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                Application.allowBlending = !Application.allowBlending;
            }
        }).togglable(Application.allowBlending), false, 0.5));
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
                UIAdjuster tb = new UIAdjuster(FontSizes.fontSizerTextHeight, field.get(), new IFunction<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) {
                        int nv = (int) (long) aLong;
                        if (nv < 1)
                            nv = 1;
                        field.accept(nv);
                        refreshLayout(false);
                        return (long) nv;
                    }
                });
                tb.accept(Integer.toString(field.get()));
                // NOTE: This is correct behavior due to an 'agreement' in FontSizes that this should be correct
                outerLayout.panelsAdd(new UISplitterLayout(new UILabel(TXDB.get("r48", field.name), FontSizes.fontSizerTextHeight), tb, false, 4, 5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
