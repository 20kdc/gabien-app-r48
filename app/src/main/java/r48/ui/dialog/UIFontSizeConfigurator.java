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
import r48.FontSizes;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;

import java.util.LinkedList;

/**
 * Created on 1/29/17.
 */
public class UIFontSizeConfigurator extends UIElement.UIProxy {
    private final UIScrollLayout outerLayout;
    private int lastFontSizerSize = -1;
    private int lastSBSize = -1;
    public final Config config;

    public UIFontSizeConfigurator(Config c) {
        config = c;
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
            if (lastFontSizerSize == config.fontSizes.fontSizerTextHeight)
                if (lastSBSize == config.fontSizes.generalScrollersize)
                    return;
        lastFontSizerSize = config.fontSizes.fontSizerTextHeight;
        lastSBSize = config.fontSizes.generalScrollersize;

        outerLayout.panelsClear();
        outerLayout.setSBSize(lastSBSize);
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
                ConfigIO.save(config);
            }
        }), new UITextButton(TXDB.get("Load"), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                ConfigIO.load(false, config);
                config.apply();
                refreshLayout(true);
            }
        }), false, 1, 2));
        UITextButton fontButton = new UITextButton("", FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                if (config.fontOverride != null) {
                    config.fontOverride = null;
                } else {
                    config.fontOverride = GaBIEn.getFontOverrides()[0];
                }
                config.apply();
            }
        }) {
            @Override
            public void updateContents(double deltaTime, boolean selected, IPeripherals peripherals) {
                text = TXDB.get("Font: ");
                if (config.fontOverride != null) {
                    text += config.fontOverride;
                } else {
                    text += TXDB.get("Internal w/fallbacks");
                }
                super.updateContents(deltaTime, selected, peripherals);
            }
        };
        final UITextButton fontButtonAppend = new UITextButton(TXDB.get("Even for height <= 8"), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
            }
        }).togglable(config.fontOverrideUE8);
        fontButtonAppend.onClick = new Runnable() {
            @Override
            public void run() {
                config.fontOverrideUE8 = fontButtonAppend.state;
                config.apply();
            }
        };
        outerLayout.panelsAdd(new UISplitterLayout(fontButton, fontButtonAppend, false, 0.5));
        String themeTxt = TXDB.get("Theme: #A").replaceAll("#A", String.valueOf(UIBorderedElement.borderTheme));
        outerLayout.panelsAdd(new UISplitterLayout(new UITextButton(themeTxt, FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                UIBorderedElement.borderTheme++;
                UIBorderedElement.borderTheme %= UIBorderedElement.BORDER_THEMES;
                refreshLayout(true);
            }
        }), new UIAppendButton(TXDB.get("External Windowing"), new UITextButton(TXDB.get("Enable Blending"), FontSizes.fontSizerTextHeight, new Runnable() {
            @Override
            public void run() {
                config.allowBlending = !config.allowBlending;
            }
        }).togglable(config.allowBlending), new Runnable() {
            @Override
            public void run() {
                config.windowingExternal = !config.windowingExternal;
            }
        }, FontSizes.fontSizerTextHeight).togglable(config.windowingExternal), false, 0.5));
        try {
            for (final FontSizes.FontSizeField field : config.fontSizes.getFields()) {
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
