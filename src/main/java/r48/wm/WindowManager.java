/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.wm;

import gabien.IDesktopPeripherals;
import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.FontSizes;
import r48.ui.Art;
import r48.ui.Coco;
import r48.ui.UINSVertLayout;

import java.util.LinkedList;

/**
 * For lack of a better place, this is a description of how window management works in R48:
 * There are 3 places windows can be.
 * They can be on the UIWindowView, on the UITabPane, or on an external window.
 * If a window is removed it either self-destructed, or the user closed it.
 * For self-destruct:
 * For the UIWindowView, TabShell automatically calls onWindowClosed.
 * The UITabPane does the same thing.
 * External windows have this passed through by the wrapper (see below)
 * For the user closing it:
 * For the UIWindowView & UITabPane, closing occurs via the icon, which can dispose as necessary.
 * External windows have a UIWindowView wrapper which:
 * 1. Acts as a display agent for context menus
 * 2. Converts any close of an immortal window into a transfer.
 * <p>
 * Coordinate conversion for context menus will work by making the assertion
 * that any pointer for context menus must be *rooted* at a UIWindowView created by WindowManager.
 * Thus, some global and local coordinates can give you a decent basis despite any transformation.
 * There's some Gabien-level modifications to IPointer that'll need to be done for this.
 * <p>
 * Created on November 14, 2018.
 */
public class WindowManager {
    public boolean creatingRealWindows;
    private Rect preFullscreenRect = null;
    private final UIWindowView rootView;
    private final UITabPane tabPane;
    private final WindowCreatingUIElementConsumer uiTicker;

    public WindowManager(UIElement topBar, final WindowCreatingUIElementConsumer uiTick) {
        uiTicker = uiTick;
        rootView = new UIWindowView() {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                if (peripherals instanceof IDesktopPeripherals)
                    Coco.run((IDesktopPeripherals) peripherals);
                super.update(deltaTime, selected, peripherals);
            }

            @Override
            public void render(IGrDriver igd) {
                super.render(igd);
            }
        };
        rootView.windowTextHeight = FontSizes.windowFrameHeight;
        rootView.sizerVisual = rootView.windowTextHeight / 2;
        rootView.sizerActual = rootView.windowTextHeight;
        rootView.setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(800), FontSizes.scaleGuess(600)));

        tabPane = new UITabPane(FontSizes.tabTextHeight, true, true);

        UIWindowView.IShell backing = new UIWindowView.ScreenShell(rootView, new UINSVertLayout(topBar, tabPane));
        rootView.addShell(backing);
        rootView.lowerShell(backing);

        uiTicker.accept(rootView, 1, false);
    }

    public void toggleFullscreen() {
        uiTicker.forceRemove(rootView);
        if (preFullscreenRect == null) {
            preFullscreenRect = rootView.getParentRelativeBounds();
            uiTicker.accept(rootView, 1, true);
        } else {
            rootView.setForcedBounds(null, preFullscreenRect);
            preFullscreenRect = null;
            uiTicker.accept(rootView, 1, false);
        }
    }

    public void selectFirstTab() {
        LinkedList<TabUtils.Tab> uie = tabPane.getTabs();
        if (uie.size() > 0)
            tabPane.selectTab(uie.getFirst().contents);
    }

    public void createWindow(final UIElement uie) {
        createWindow(uie, false, false);
    }

    public void createWindow(final UIElement uie, final boolean tab, final boolean immortal) {
        if (tab) {
            if (immortal) {
                tabPane.addTab(new TabUtils.Tab(uie, new TabUtils.TabIcon[] {
                        new TabUtils.TabIcon() {
                            @Override
                            public void draw(IGrDriver igd, int x, int y, int size) {
                                Art.windowWindowIcon(igd, x, y, size);
                            }

                            @Override
                            public void click(TabUtils.Tab self) {
                                tabPane.removeTab(self);
                                Size mainSize = getRootSize();
                                uie.setForcedBounds(null, new Rect(0, 0, mainSize.width / 2, mainSize.height / 2));
                                createWindow(uie, false, true);
                            }
                        }
                }));
            } else {
                tabPane.addTab(new TabUtils.Tab(uie, new TabUtils.TabIcon[] {
                        new TabUtils.TabIcon() {
                            @Override
                            public void draw(IGrDriver igd, int x, int y, int size) {
                                Art.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
                            }

                            @Override
                            public void click(TabUtils.Tab self) {
                                tabPane.removeTab(self);
                                // Since this was manually removed, this must be called manually.
                                uie.onWindowClose();
                            }
                        },
                        new TabUtils.TabIcon() {
                            @Override
                            public void draw(IGrDriver igd, int x, int y, int size) {
                                Art.windowWindowIcon(igd, x, y, size);
                            }

                            @Override
                            public void click(TabUtils.Tab self) {
                                tabPane.removeTab(self);
                                Size mainSize = getRootSize();
                                uie.setForcedBounds(null, new Rect(0, 0, mainSize.width / 2, mainSize.height / 2));
                                createWindow(uie, false, true);
                            }
                        }
                }));
                tabPane.selectTab(uie);
            }
        } else {
            if (creatingRealWindows) {
                // This is going to be done once Shells are in the 'SHL' form rather than their present form
            } else {
                if (immortal) {
                    rootView.addShell(new UIWindowView.TabShell(rootView, uie, new TabUtils.TabIcon[] {
                            new TabUtils.TabIcon() {
                                @Override
                                public void draw(IGrDriver igd, int x, int y, int size) {
                                    Art.tabWindowIcon(igd, x, y, size);
                                }

                                @Override
                                public void click(TabUtils.Tab tab) {
                                    rootView.removeTab(tab);
                                    createWindow(uie, true, true);
                                }
                            }
                    }));
                } else {
                    rootView.addShell(new UIWindowView.TabShell(rootView, uie, new TabUtils.TabIcon[] {
                            new TabUtils.TabIcon() {
                                @Override
                                public void draw(IGrDriver igd, int x, int y, int size) {
                                    Art.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
                                }

                                @Override
                                public void click(TabUtils.Tab tab) {
                                    rootView.removeTab(tab);
                                    // We are actually closing (this isn't called by default for a remotely triggered remove)
                                    uie.onWindowClose();
                                }
                            },
                            new TabUtils.TabIcon() {
                                @Override
                                public void draw(IGrDriver igd, int x, int y, int size) {
                                    Art.tabWindowIcon(igd, x, y, size);
                                }

                                @Override
                                public void click(TabUtils.Tab tab) {
                                    rootView.removeTab(tab);
                                    createWindow(uie, true, false);
                                }
                            }
                    }));
                }
            }
        }
    }

    public Size getRootSize() {
        return rootView.getSize();
    }

    public void setOrange(double v) {
        tabPane.visualizationOrange = v;
    }
}
