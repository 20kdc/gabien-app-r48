/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import gabien.GaBIEn;
import gabien.IPeripherals;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.cfg.FontSizes.FontSizeField;
import r48.tr.pages.TrRoot;
import r48.ui.UIAppendButton;

import java.util.LinkedList;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIElement.UIProxy {
    private final UIScrollLayout outerLayout;
    private int lastFontSizerSize = -1;
    private int lastSBSize = -1;
    public final Config c;
    public final TrRoot T;
    public final Runnable apply;

    public UIFontSizeConfigurator(Config c, TrRoot t, Runnable apply) {
        this.c = c;
        T = t;
        this.apply = apply;
        outerLayout = new UIScrollLayout(true, c.f.generalScrollersize);
        refreshLayout(true);
        proxySetElement(outerLayout, false);
        setForcedBounds(null, new Rect(0, 0, c.f.scaleGuess(320), c.f.scaleGuess(240)));
    }

    /**
     * REPLACE WITH TrGlobal FIELDS
     */
    @Deprecated
    public String TEMP(String t) {
        return t;
    }

    public void refreshLayout(boolean force) {
        double iniScroll = 0;
        if (outerLayout != null)
            iniScroll = outerLayout.scrollbar.scrollPoint;
        if (!force)
            if (lastFontSizerSize == c.f.fontSizerTextHeight)
                if (lastSBSize == c.f.generalScrollersize)
                    return;
        lastFontSizerSize = c.f.fontSizerTextHeight;
        lastSBSize = c.f.generalScrollersize;

        outerLayout.panelsClear();
        outerLayout.setSBSize(lastSBSize);
        outerLayout.scrollbar.scrollPoint = iniScroll;
        final LinkedList<Runnable> doubleAll = new LinkedList<Runnable>();
        final LinkedList<Runnable> halfAll = new LinkedList<Runnable>();
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton("*2", c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                for (Runnable r : doubleAll)
                    r.run();
                apply.run();
                refreshLayout(false);
            }
        }), new UITextButton("/2", c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                for (Runnable r : halfAll)
                    r.run();
                apply.run();
                refreshLayout(false);
            }
        }), false, 1, 2));
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton(T.g.wordSave, c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                ConfigIO.save(c);
            }
        }), new UITextButton(T.g.wordLoad, c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                ConfigIO.load(false, c);
                apply.run();
                refreshLayout(true);
            }
        }), false, 1, 2));
        UITextButton fontButton = new UITextButton("", c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                if (c.fontOverride != null) {
                    c.fontOverride = null;
                } else {
                    c.fontOverride = GaBIEn.getFontOverrides()[0];
                }
                apply.run();
            }
        }) {
            @Override
            public void updateContents(double deltaTime, boolean selected, IPeripherals peripherals) {
                text = TEMP("Font: ");
                if (c.fontOverride != null) {
                    text += c.fontOverride;
                } else {
                    text += TEMP("Internal w/fallbacks");
                }
                super.updateContents(deltaTime, selected, peripherals);
            }
        };
        final UITextButton fontButtonAppend = new UITextButton(TEMP("Even for height <= 8"), c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
            }
        }).togglable(c.fontOverrideUE8);
        fontButtonAppend.onClick = new Runnable() {
            @Override
            public void run() {
                c.fontOverrideUE8 = fontButtonAppend.state;
                apply.run();
            }
        };
        outerLayout.panelsAdd(new UISplitterLayout(fontButton, fontButtonAppend, false, 0.5));
        String themeTxt = TEMP("Theme: #A").replaceAll("#A", String.valueOf(c.borderTheme));
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton(themeTxt, c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                c.borderTheme++;
                c.borderTheme %= UIBorderedElement.BORDER_THEMES;
                apply.run();
                refreshLayout(true);
            }
        }), new UIAppendButton(TEMP("External Windowing"), new UITextButton(TEMP("Enable Blending"), c.f.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                c.allowBlending = !c.allowBlending;
            }
        }).togglable(c.allowBlending), new Runnable() {
            @Override
            public void run() {
                c.windowingExternal = !c.windowingExternal;
            }
        }, c.f.fontSizerTextHeight).togglable(c.windowingExternal), false, 0.5));
        try {
            for (final FontSizeField field : c.f.getFields()) {
                doubleAll.add(new Runnable() {
                    @Override
                    public void run() {
                        int v = field.get() * 2;
                        if (v == 12)
                            v = 8;
                        field.accept(v);
                    }
                });
                halfAll.add(new Runnable() {
                    @Override
                    public void run() {
                        int v = field.get() / 2;
                        int min = 6;
                        if (field.name.equals("windowFrameHeight"))
                            min = 8;
                        if (field.name.equals("tabTextHeight"))
                            min = 8;
                        if (v < min)
                            v = min;
                        if (field.name.equals("statusBarTextHeight"))
                            if (v == 8)
                                v = 6;
                        field.accept(v);
                    }
                });
                UIAdjuster tb = new UIAdjuster(c.f.fontSizerTextHeight, field.get(), new IFunction<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) {
                        int nv = (int) (long) aLong;
                        if (nv < 1)
                            nv = 1;
                        field.accept(nv);
                        apply.run();
                        refreshLayout(false);
                        return (long) nv;
                    }
                });
                tb.accept(Integer.toString(field.get()));
                // NOTE: This is correct behavior due to an 'agreement' in FontSizes that this should be correct
                outerLayout.panelsAdd(new UISplitterLayout(new UILabel(field.trName(T.fontSizes), c.f.fontSizerTextHeight), tb, false, 4, 5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
