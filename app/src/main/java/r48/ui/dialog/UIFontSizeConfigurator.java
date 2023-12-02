/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dialog;

import gabien.GaBIEn;
import gabien.ui.*;
import gabien.ui.elements.UIAdjuster;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.cfg.FontSizes.FontSizeField;
import r48.tr.pages.TrRoot;

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
        outerLayout = new UIScrollLayout(true, c.f.generalS);
        refreshLayout(true);
        proxySetElement(outerLayout, false);
        setForcedBounds(null, new Rect(0, 0, c.f.scaleGuess(320), c.f.scaleGuess(240)));
    }

    public void refreshLayout(boolean force) {
        double iniScroll = 0;
        if (outerLayout != null)
            iniScroll = outerLayout.scrollbar.scrollPoint;
        if (!force)
            if (lastFontSizerSize == c.f.fontSizerTH)
                if (lastSBSize == c.f.generalS)
                    return;
        lastFontSizerSize = c.f.fontSizerTH;
        lastSBSize = c.f.generalS;

        LinkedList<UIElement> elms = new LinkedList<>();
        outerLayout.scrollbar.scrollPoint = iniScroll;
        final LinkedList<Runnable> doubleAll = new LinkedList<Runnable>();
        final LinkedList<Runnable> halfAll = new LinkedList<Runnable>();
        elms.add(new UISplitterLayout(new UITextButton("*2", c.f.fontSizerTH, () -> {
            for (Runnable r : doubleAll)
                r.run();
            apply.run();
            refreshLayout(false);
        }), new UITextButton("/2", c.f.fontSizerTH, () -> {
            for (Runnable r : halfAll)
                r.run();
            apply.run();
            refreshLayout(false);
        }), false, 1, 2));
        elms.add(new UISplitterLayout(new UITextButton(T.g.wordSave, c.f.fontSizerTH, () -> {
            ConfigIO.save(c);
        }), new UITextButton(T.g.wordLoad, c.f.fontSizerTH, () -> {
            ConfigIO.load(false, c);
            apply.run();
            refreshLayout(true);
        }), false, 1, 2));
        UITextButton fontButton = new UITextButton("", c.f.fontSizerTH, () -> {
            if (c.fontOverride != null) {
                c.fontOverride = null;
            } else {
                c.fontOverride = GaBIEn.getFontOverrides()[0];
            }
            apply.run();
        }) {
            @Override
            public void updateContents(double deltaTime, boolean selected, IPeripherals peripherals) {
                String fName;
                if (c.fontOverride != null) {
                    fName = c.fontOverride;
                } else {
                    fName = T.g.fsc_fontInternal;
                }
                setText(T.g.fsc_font.r(fName));
                super.updateContents(deltaTime, selected, peripherals);
            }
        };
        final UITextButton fontButtonAppend = new UITextButton(T.g.fsc_fontEvenSmall, c.f.fontSizerTH, null)
                .togglable(c.fontOverrideUE8);
        fontButtonAppend.onClick = () -> {
            c.fontOverrideUE8 = fontButtonAppend.state;
            apply.run();
        };
        elms.add(new UISplitterLayout(fontButton, fontButtonAppend, false, 0.5));
        elms.add(new UISplitterLayout(new UITextButton("", c.f.fontSizerTH, () -> {
            c.borderTheme++;
            apply.run();
        }) {
            @Override
            public void updateContents(double deltaTime, boolean selected, IPeripherals peripherals) {
                setText(T.g.fsc_theme.r(c.borderTheme));
                super.updateContents(deltaTime, selected, peripherals);
            }
        }, new UITextButton(T.g.fsc_externalWindowing, c.f.fontSizerTH, () -> {
            c.windowingExternal = !c.windowingExternal;
        }).togglable(c.windowingExternal), false, 0.5));
        for (final FontSizeField field : c.f.fields) {
            doubleAll.add(() -> {
                int v = field.get() * 2;
                if (v == 12)
                    v = 8;
                field.accept(v);
            });
            halfAll.add(() -> {
                int v = field.get() / 2;
                if (v < field.minValue)
                    v = field.minValue;
                field.accept(v);
            });
            UIAdjuster tb = new UIAdjuster(c.f.fontSizerTH, field.get(), (aLong) -> {
                int nv = (int) (long) aLong;
                if (nv < 1)
                    nv = 1;
                field.accept(nv);
                apply.run();
                refreshLayout(false);
                return (long) nv;
            });
            tb.accept(Integer.toString(field.get()));
            // NOTE: This is correct behavior due to an 'agreement' in FontSizes that this should be correct
            elms.add(new UISplitterLayout(new UILabel(field.trName(T.fontSizes), c.f.fontSizerTH), tb, false, 4, 5));
        }
        outerLayout.panelsSet(elms);
    }
}
