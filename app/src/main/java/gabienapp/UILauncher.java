/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

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
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.spacing.UIBorderedSubpanel;

/**
 * Split from Application on 24th August 2022.
 */
public class UILauncher extends UIProxy {
    private final AtomicBoolean gamepaksRequestClose;
    public UILauncher(final AtomicBoolean grc) {
        gamepaksRequestClose = grc;
        final UITabPane tabPane = new UITabPane(FontSizes.tabTextHeight, false, false);

        final UIScrollLayout configure = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public String toString() {
                return TXDB.get("Configure");
            }
        };
        tabPane.addTab(new Tab(configure, new TabIcon[0]));

        final UIScrollLayout gamepaks = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public String toString() {
                return TXDB.get("Select Engine");
            }
        };
        tabPane.addTab(new Tab(gamepaks, new TabIcon[0]));

        // this can't be good
        // Ok, explaination for this. Giving it a runnable, it will hold it until called again, and then it will run it and remove it.
        final IConsumer<Runnable> closeHelper = new IConsumer<Runnable>() {
            private Runnable r;

            @Override
            public void accept(Runnable runnable) {
                if (runnable != null) {
                    r = runnable;
                } else {
                    r.run();
                    r = null;
                }
            }
        };

        UIAdjuster msAdjust = new UIAdjuster(FontSizes.launcherTextHeight, Application.globalMS, new IFunction<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                int gms = (int) (long) aLong;
                if (gms < 1)
                    gms = 1;
                Application.globalMS = gms;
                return (long) gms;
            }
        });
        msAdjust.accept(Integer.toString(Application.globalMS));

        final LinkedList<UIElement> basePanels = new LinkedList<UIElement>();

        UIHelpSystem uhs = new UIHelpSystem();
        HelpSystemController hsc = new HelpSystemController(null, "Help/Launcher/Entry", uhs);
        hsc.loadPage(0);

        configure.panelsAdd(new UIBorderedSubpanel(uhs, FontSizes.scaleGuess(8)));

        configure.panelsAdd(figureOutTopBar(Application.uiTicker, closeHelper));

        configure.panelsAdd(new UISplitterLayout(new UILabel(TXDB.get("MS per frame:"), FontSizes.launcherTextHeight), msAdjust, false, 3, 5));

        configure.panelsAdd(new UILabel(TXDB.get("Path To Game (if you aren't running R48 in the game folder):"), FontSizes.launcherTextHeight));

        final UIGamePathList rootBox = new UIGamePathList(Application.rootPathBackup) {
            @Override
            public void modified() {
                super.modified();
                FontSizes.save();
            }
        };
        configure.panelsAdd(rootBox);

        configure.panelsAdd(new UILabel(TXDB.get("Secondary Image Load Location:"), FontSizes.launcherTextHeight));

        final UIGamePathList sillBox = new UIGamePathList(Application.secondaryImageLoadLocationBackup) {
            @Override
            public void modified() {
                super.modified();
                FontSizes.save();
            }
        };
        configure.panelsAdd(sillBox);

        basePanels.add(new UILabel(TXDB.get("Choose Target Engine:"), FontSizes.launcherTextHeight));

        final IConsumer<IGPMenuPanel> menuConstructor = new IConsumer<IGPMenuPanel>() {
            @Override
            public void accept(IGPMenuPanel igpMenuPanel) {
                gamepaks.panelsClear();
                for (UIElement uie : basePanels)
                    gamepaks.panelsAdd(uie);
                if (igpMenuPanel == null) {
                    closeHelper.accept(null);
                    return;
                }
                String[] names = igpMenuPanel.getButtonText();
                IFunction<LauncherState, IGPMenuPanel>[] runs = igpMenuPanel.getButtonActs();
                for (int i = 0; i < names.length; i++) {
                    final IFunction<LauncherState, IGPMenuPanel> r = runs[i];
                    gamepaks.panelsAdd(new UITextButton(names[i], FontSizes.launcherTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            accept(r.apply(new LauncherState(rootBox.text.text, sillBox.text.text)));
                        }
                    }));
                }
            }
        };

        configure.panelsAdd(new UISplitterLayout(new UIPublicPanel(0, 0), new UITextButton(TXDB.get("Continue"), FontSizes.launcherTextHeight, new Runnable() {
            @Override
            public void run() {
                tabPane.selectTab(gamepaks);
            }
        }), false, 1));
        // ...

        tabPane.setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(640), FontSizes.scaleGuess(480)));
        menuConstructor.accept(new PrimaryGPMenuPanel());
        closeHelper.accept(new Runnable() {
            @Override
            public void run() {
                gamepaksRequestClose.set(true);
            }
        });
        proxySetElement(tabPane, false);
    }

    @Override
    public boolean requestsUnparenting() {
        return gamepaksRequestClose.get();
    }

    private static UIElement figureOutTopBar(final WindowCreatingUIElementConsumer uiTicker, final IConsumer<Runnable> closeHelper) {
        UIElement whatever = new UITextButton(TXDB.get("Quit R48"), FontSizes.launcherTextHeight, new Runnable() {
            @Override
            public void run() {
                GaBIEn.ensureQuit();
            }
        });
        if (!GaBIEn.singleWindowApp()) { // SWA means we can't create windows
            whatever = new UISplitterLayout(whatever, new UITextButton(TXDB.get("Configuration"), FontSizes.launcherTextHeight, new Runnable() {
                @Override
                public void run() {
                    uiTicker.accept(new UIFontSizeConfigurator());
                    closeHelper.accept(null);
                }
            }), false, 1, 2);
        }

        return new UIAppendButton(TXDB.getLanguage(), whatever, new Runnable() {
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
                TXDB.nextLanguage();
                closeHelper.accept(null);
            }
        }, FontSizes.launcherTextHeight);
    }
}
