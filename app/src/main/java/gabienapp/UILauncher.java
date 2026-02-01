/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.function.Function;

import static datum.DatumTreeUtils.*;

import datum.DatumSrcLoc;
import datum.DatumVisitor;
import datum.DatumWriter;
import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.dialogs.UICredits;
import gabien.ui.elements.UIAdjuster;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabPane;
import gabien.ui.layouts.UITabBar.Tab;
import gabien.ui.layouts.UITabBar.TabIcon;
import gabien.uslx.append.Rect;
import gabien.wsi.IDesktopPeripherals;
import gabien.wsi.IGrInDriver;
import gabien.wsi.IPeripherals;
import gabienapp.state.LSInApp;
import gabienapp.state.LSMain;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.ioplus.DatumLoader;
import r48.tr.LanguageList;
import r48.tr.pages.TrGlobal;
import r48.ui.UIAppendButton;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.spacing.UIBorderedSubpanel;

/**
 * Split from Application on 24th August 2022.
 */
public class UILauncher extends UIProxy {
    private boolean gamepaksRequestClose = false;
    public final UIGamePathList rootBox, sillBox;

    private final UIScrollLayout gamepaks;
    private final Config c;
    private int counterForTree;
    private final TrGlobal tr;

    public UILauncher(final LSMain ls) {
        final Launcher lun = ls.lun;
        tr = lun.ilg.t.g;
        c = lun.c;
        final UITabPane tabPane = new UITabPane(c.f.tabTH, false, false);

        final UIScrollLayout configure = new UIScrollLayout(true, c.f.generalS) {
            @Override
            public String toString() {
                return tr.bConfigV;
            }
        };
        tabPane.addTab(new Tab(configure, new TabIcon[0]));

        gamepaks = new UIScrollLayout(true, c.f.generalS) {
            @Override
            public String toString() {
                return tr.bSelectEngine;
            }
        };
        tabPane.addTab(new Tab(gamepaks, new TabIcon[0]));

        UICredits credits = new UICredits(c.f.generalS, c.f.launcherTH) {
            @Override
            public String toString() {
                return tr.bCredits;
            }
        };
        tabPane.addTab(new Tab(credits, new TabIcon[0]));

        UIAdjuster msAdjust = new UIAdjuster(c.f.launcherTH, lun.globalMS, new Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                int gms = (int) (long) aLong;
                if (gms < 1)
                    gms = 1;
                lun.globalMS = gms;
                return (long) gms;
            }
        });
        msAdjust.accept(Integer.toString(lun.globalMS));

        UIHelpSystem uhs = new UIHelpSystem(lun.ilg);
        HelpSystemController hsc = new HelpSystemController(null, "Help/Launcher/Entry", uhs);
        hsc.loadPage(0);

        rootBox = new UIGamePathList(c, c.rootPathBackup) {
            @Override
            public void modified() {
                super.modified();
                ConfigIO.save(c);
            }
        };

        sillBox = new UIGamePathList(c, c.secondaryImageLoadLocationBackup) {
            @Override
            public void modified() {
                super.modified();
                ConfigIO.save(c);
            }
        };

        configure.panelsSet(
                new UIBorderedSubpanel(uhs, c.f.scaleGuess(8)),
                figureOutTopBar(lun),
                new UISplitterLayout(new UILabel(tr.lFrameMS, c.f.launcherTH), msAdjust, false, 3, 5),
                new UILabel(tr.lGamePath, c.f.launcherTH),
                rootBox,
                new UILabel(tr.lSecondaryPath, c.f.launcherTH),
                sillBox,
                new UISplitterLayout(new UIEmpty(), new UITextButton(tr.bContinue, c.f.launcherTH, () -> {
                    tabPane.selectTab(gamepaks);
                }), false, 1));
        // ...

        tabPane.setForcedBounds(null, new Rect(0, 0, c.f.scaleGuess(640), c.f.scaleGuess(480)));
        // setup initial panel by creating an outer visitor, visiting a list within it, and then continue within that
        DatumVisitor visitor = decVisitor((res, srcLoc) -> {
            setPanel(LauncherEntry.makeFrom(null, ls, cList(res)));
        });
        DatumVisitor visitor2 = visitor.visitList(DatumSrcLoc.NONE);
        DatumLoader.readEssential("gamepaks", null, visitor2);
        visitor2.visitEnd(DatumSrcLoc.NONE);
        // done!
        proxySetElement(tabPane, false);
    }

    @Override
    public boolean requestsUnparenting() {
        return gamepaksRequestClose;
    }

    public void requestClose() {
        gamepaksRequestClose = true;
    }

    public void setPanel(LinkedList<LauncherEntry> igpMenuPanel) {
        LinkedList<UIElement> elms = new LinkedList<>();
        elms.add(new UILabel(tr.lChooseEngine, c.f.launcherTH));
        if (igpMenuPanel == null) {
            gamepaksRequestClose = true;
            return;
        }
        for (LauncherEntry panel : igpMenuPanel)
            elms.add(new UITextButton(panel.name.r(), c.f.launcherTH, panel.runnable));
        gamepaks.panelsSet(elms);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        if (peripherals instanceof IDesktopPeripherals) {
            IDesktopPeripherals dph = (IDesktopPeripherals) peripherals;
            // UI debugging feature
            if (dph.isKeyDown(IGrInDriver.VK_CONTROL) && dph.isKeyJustPressed(IGrInDriver.VK_I)) {
                counterForTree++;
                if (counterForTree == 3) {
                    counterForTree = 0;
                    StringWriter sw = new StringWriter();
                    DatumWriter dw = new DatumWriter(sw);
                    debugDumpUITree(dw);
                    GaBIEn.clipboard.copyText(sw.toString());
                }
            }
        }
    }

    private UIElement figureOutTopBar(final Launcher lun) {
        final Config c = lun.c;
        final TrGlobal tr = lun.ilg.t.g;
        final WindowCreatingUIElementConsumer uiTicker = lun.uiTicker;
        UIElement whatever = new UITextButton(tr.bQuit, c.f.launcherTH, () -> {
            GaBIEn.ensureQuit();
        });
        if (!GaBIEn.singleWindowApp()) { // SWA means we can't create windows
            whatever = new UISplitterLayout(whatever, new UITextButton(tr.bConfigN, c.f.launcherTH, () -> {
                UIFontSizeConfigurator usc = new UIFontSizeConfigurator(c, lun.ilg.t, () -> {
                    c.applyUIGlobals();
                });
                uiTicker.accept(usc);
                lun.currentState = new LSInApp(lun);
                gamepaksRequestClose = true;
            }), false, 1, 2);
        }

        return new UIAppendButton(lun.c.language, whatever, () -> {
            // Unfortunately, if done quickly enough, the font will not load in time.
            // (Java "lazily" loads fonts.
            //  gabien-javase works around this bug - lazy loading appears to result in Java devs not caring about font load speed -
            //  and by the time it matters it's usually loaded, but, well, suffice to say this hurts my translatability plans a little.
            //  Not that it'll stop them, but it's annoying.)
            // This associates a lag with switching language, when it's actually due to Java being slow at loading a font.
            // (I'm slightly glad I'm not the only one this happens for, but unhappy that it's an issue.)
            // Unfortunately, a warning message cannot be shown to the user, as the warning message would itself trigger lag-for-font-load.
            c.language = LanguageList.getNextLanguage(c.language).id;
            lun.ilg.updateLanguage((str) -> {
                // one can imagine the splash screen being used here, but not for now
            });
            lun.currentState = new LSMain(lun);
            gamepaksRequestClose = true;
        }, c.f.launcherTH);
    }
}
