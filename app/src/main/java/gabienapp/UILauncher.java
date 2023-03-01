/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.LinkedList;

import gabien.GaBIEn;
import gabien.ui.Rect;
import gabien.ui.UIAdjuster;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIPublicPanel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITabPane;
import gabien.ui.UITextButton;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabien.ui.UIElement.UIProxy;
import gabien.ui.UITabBar.Tab;
import gabien.ui.UITabBar.TabIcon;
import gabien.uslx.append.IConsumer;
import gabien.uslx.append.IFunction;
import gabienapp.IGPMenuPanel.LauncherState;
import gabienapp.state.LSMain;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.tr.LanguageList;
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
    public UILauncher(final LSMain ls) {
        final Launcher lun = ls.lun;
        Config c = lun.c;
        final UITabPane tabPane = new UITabPane(c.f.tabTextHeight, false, false);

        final UIScrollLayout configure = new UIScrollLayout(true, c.f.generalScrollersize) {
            @Override
            public String toString() {
                return ls.tr("Configure");
            }
        };
        tabPane.addTab(new Tab(configure, new TabIcon[0]));

        final UIScrollLayout gamepaks = new UIScrollLayout(true, c.f.generalScrollersize) {
            @Override
            public String toString() {
                return ls.tr("Select Engine");
            }
        };
        tabPane.addTab(new Tab(gamepaks, new TabIcon[0]));

        UIAdjuster msAdjust = new UIAdjuster(c.f.launcherTextHeight, lun.globalMS, new IFunction<Long, Long>() {
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

        final LinkedList<UIElement> basePanels = new LinkedList<UIElement>();

        UIHelpSystem uhs = new UIHelpSystem(lun.ilg);
        HelpSystemController hsc = new HelpSystemController(null, "Help/Launcher/Entry", uhs);
        hsc.loadPage(0);

        configure.panelsAdd(new UIBorderedSubpanel(uhs, c.f.scaleGuess(8)));

        configure.panelsAdd(figureOutTopBar(lun));

        configure.panelsAdd(new UISplitterLayout(new UILabel(ls.tr("MS per frame:"), c.f.launcherTextHeight), msAdjust, false, 3, 5));

        configure.panelsAdd(new UILabel(ls.tr("Path To Game (if you aren't running R48 in the game folder):"), c.f.launcherTextHeight));

        final UIGamePathList rootBox = new UIGamePathList(c, c.rootPathBackup) {
            @Override
            public void modified() {
                super.modified();
                ConfigIO.save(c);
            }
        };
        configure.panelsAdd(rootBox);

        configure.panelsAdd(new UILabel(ls.tr("Secondary Image Load Location:"), c.f.launcherTextHeight));

        final UIGamePathList sillBox = new UIGamePathList(c, c.secondaryImageLoadLocationBackup) {
            @Override
            public void modified() {
                super.modified();
                ConfigIO.save(c);
            }
        };
        configure.panelsAdd(sillBox);

        basePanels.add(new UILabel(ls.tr("Choose Target Engine:"), c.f.launcherTextHeight));

        final IConsumer<IGPMenuPanel> menuConstructor = new IConsumer<IGPMenuPanel>() {
            @Override
            public void accept(IGPMenuPanel igpMenuPanel) {
                gamepaks.panelsClear();
                for (UIElement uie : basePanels)
                    gamepaks.panelsAdd(uie);
                if (igpMenuPanel == null) {
                    gamepaksRequestClose = true;
                    return;
                }
                String[] names = igpMenuPanel.getButtonText();
                IFunction<LauncherState, IGPMenuPanel>[] runs = igpMenuPanel.getButtonActs();
                for (int i = 0; i < names.length; i++) {
                    final IFunction<LauncherState, IGPMenuPanel> r = runs[i];
                    gamepaks.panelsAdd(new UITextButton(names[i], c.f.launcherTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            accept(r.apply(new LauncherState(rootBox.text.text, sillBox.text.text)));
                        }
                    }));
                }
            }
        };

        configure.panelsAdd(new UISplitterLayout(new UIPublicPanel(0, 0), new UITextButton(ls.tr("Continue"), c.f.launcherTextHeight, new Runnable() {
            @Override
            public void run() {
                tabPane.selectTab(gamepaks);
            }
        }), false, 1));
        // ...

        tabPane.setForcedBounds(null, new Rect(0, 0, c.f.scaleGuess(640), c.f.scaleGuess(480)));
        menuConstructor.accept(new PrimaryGPMenuPanel(ls));
        proxySetElement(tabPane, false);
    }

    @Override
    public boolean requestsUnparenting() {
        return gamepaksRequestClose;
    }

    private UIElement figureOutTopBar(final Launcher lun) {
        final Config c = lun.c;
        final WindowCreatingUIElementConsumer uiTicker = lun.uiTicker;
        UIElement whatever = new UITextButton(lun.ilg.tr("Quit R48"), c.f.launcherTextHeight, new Runnable() {
            @Override
            public void run() {
                GaBIEn.ensureQuit();
            }
        });
        if (!GaBIEn.singleWindowApp()) { // SWA means we can't create windows
            whatever = new UISplitterLayout(whatever, new UITextButton(lun.ilg.tr("Configuration"), c.f.launcherTextHeight, new Runnable() {
                @Override
                public void run() {
                    uiTicker.accept(new UIFontSizeConfigurator(c, () -> {
                        c.applyUIGlobals();
                    }));
                    gamepaksRequestClose = true;
                }
            }), false, 1, 2);
        }

        return new UIAppendButton(lun.c.language, whatever, new Runnable() {
            @Override
            public void run() {
                // Unfortunately, if done quickly enough, the font will not load in time.
                // (Java "lazily" loads fonts.
                //  gabien-javase works around this bug - lazy loading appears to result in Java devs not caring about font load speed -
                //  and by the time it matters it's usually loaded, but, well, suffice to say this hurts my translatability plans a little.
                //  Not that it'll stop them, but it's annoying.)
                // This associates a lag with switching language, when it's actually due to Java being slow at loading a font.
                // (I'm slightly glad I'm not the only one this happens for, but unhappy that it's an issue.)
                // Unfortunately, a warning message cannot be shown to the user, as the warning message would itself trigger lag-for-font-load.
                c.language = LanguageList.getNextLanguage(c.language);
                lun.ilg.updateLanguage((str) -> {
                    // one can imagine the splash screen being used here, but not for now
                });
                lun.currentState = new LSMain(lun);
                gamepaksRequestClose = true;
            }
        }, c.f.launcherTextHeight);
    }
}
