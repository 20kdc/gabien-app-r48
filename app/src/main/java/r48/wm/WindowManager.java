/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.wm;

import gabien.*;
import gabien.datum.DatumWriter;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.*;
import gabien.ui.elements.UIBorderedElement;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UITabPane;
import gabien.ui.layouts.UIWindowView;
import gabien.ui.layouts.UIWindowView.TabShell;
import gabien.ui.theming.Theme;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import gabien.wsi.IDesktopPeripherals;
import gabien.wsi.IPeripherals;
import gabien.wsi.IPointer;
import r48.App;
import r48.app.AppCore;
import r48.ui.Art;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

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
public class WindowManager extends AppCore.Csv {
    private Rect preFullscreenRect = null;
    // 'protected' elements here are accessible to the testing framework.
    private final UIWindowView rootView;
    protected final UITabPane tabPane;
    private final TrackedUITicker uiTicker;
    // It is required by the test system that all UIWindowViews be roots of their own windows.
    protected final LinkedList<UIWindowView> allWindowViews = new LinkedList<UIWindowView>();
    private IImage modImg;
    private boolean performingScreenTransfer = true;
    public final HashMap<String, Rect> recordedWindowPositions = new HashMap<String, Rect>();
    public final Coco coco;

    public WindowManager(App app, final WindowCreatingUIElementConsumer uiTick, UIElement thbrL, UIElement thbrR) {
        super(app);
        coco = new Coco(app);
        uiTicker = new TrackedUITicker(uiTick);
        modImg = GaBIEn.createImage(new int[] {0x80000000}, 1, 1);
        rootView = new UIWindowView() {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                if (peripherals instanceof IDesktopPeripherals)
                    coco.run((IDesktopPeripherals) peripherals);
                uiTicker.shakeOffDeadWindows();
                super.update(deltaTime, selected, peripherals);
            }

            @Override
            public void onWindowClose() {
                // This has nasty side effects if the window is merely being transferred around, so check for that.
                if (!performingScreenTransfer)
                    super.onWindowClose();
            }
        };
        rootView.windowTextHeight = app.f.windowFrameH;
        rootView.sizerVisual = rootView.windowTextHeight / 2;
        rootView.sizerActual = rootView.windowTextHeight;
        rootView.setForcedBounds(null, new Rect(0, 0, app.f.scaleGuess(800), app.f.scaleGuess(600)));

        tabPane = new UITabPane(app.f.tabTH, true, true, app.f.maintabsS, thbrL, thbrR);

        UIWindowView.IShell backing = new UIWindowView.ScreenShell(rootView, tabPane);
        rootView.addShell(backing);
        rootView.lowerShell(backing);

        allWindowViews.add(rootView);
    }

    // BEWARE: Presently closes even non-App windows
    public void pleaseShutdown() {
        for (UIElement uie : uiTicker.runningWindows())
            uiTicker.forceRemove(uie);
    }

    public void finishInitialization() {
        if (!performingScreenTransfer)
            throw new RuntimeException("That's not supposed to happen");
        uiTicker.accept(rootView, false);
        performingScreenTransfer = false;
    }

    public void toggleFullscreen() {
        performingScreenTransfer = true;

        uiTicker.forceRemove(rootView);
        if (preFullscreenRect == null) {
            preFullscreenRect = rootView.getParentRelativeBounds();
            uiTicker.accept(rootView, true);
        } else {
            rootView.setForcedBounds(null, preFullscreenRect);
            preFullscreenRect = null;
            uiTicker.accept(rootView, false);
        }

        performingScreenTransfer = false;
    }

    public void selectFirstTab() {
        LinkedList<UITabBar.Tab> uie = tabPane.getTabs();
        if (uie.size() > 0)
            tabPane.selectTab(uie.getFirst().contents);
    }

    public void adjustWindowSH(final UIElement mtb) {
        Size rootSize = getRootSize();
        Size validSize = new Size((rootSize.width * 3) / 4, (rootSize.height * 3) / 4);
        int recWidth = mtb.getWantedSize().width;

        // As of UI rewrite, this logic is much clearer
        int w = Math.min(recWidth, validSize.width);
        int recHeight = mtb.layoutGetHForW(w);
        int h = Math.min(recHeight, validSize.height);

        mtb.setForcedBounds(null, new Rect(0, 0, w, h));
    }

    public void createWindowSH(final UIElement mtb) {
        adjustWindowSH(mtb);
        createWindow(mtb);
    }

    public void createWindow(final UIElement uie) {
        createWindow(uie, false, false);
    }

    public void createWindow(final UIElement uie, final @Nullable String disposition) {
        createWindow(uie, false, false, disposition);
    }

    public void createWindow(final UIElement uie, final boolean tab, final boolean immortal) {
        createWindow(uie, tab, immortal, uie.getClass().getSimpleName());
    }

    public void createWindow(final UIElement uie, final boolean tab, final boolean immortal, final @Nullable String disposition) {
        // Now decide what to actually do.
        LinkedList<UITabBar.TabIcon> icons = new LinkedList<>();
        if (uie instanceof IDuplicatableWindow) {
            // clone frame!
            icons.add(new UITabBar.TabIcon() {
                @Override
                public void draw(IGrDriver igd, int x, int y, int size) {
                    app.a.drawSymbol(igd, Art.Symbol.CloneFrame, x, y, size, false, false);
                }

                @Override
                public void click(UITabBar.Tab self) {
                    ((IDuplicatableWindow) uie).duplicateThisWindow();
                }
            });
        }
        if (tab) {
            icons.addFirst(new UITabBar.TabIcon() {
                @Override
                public void draw(IGrDriver igd, int x, int y, int size) {
                    Art.windowWindowIcon(igd, x, y, size);
                }

                @Override
                public void click(UITabBar.Tab self) {
                    tabPane.removeTab(self);
                    Size mainSize = getRootSize();
                    uie.setForcedBounds(null, new Rect(0, 0, mainSize.width / 2, mainSize.height / 2));
                    createWindow(uie, false, immortal);
                }
            });
            if (!immortal) {
                icons.addFirst(new UITabBar.TabIcon() {
                    @Override
                    public void draw(IGrDriver igd, int x, int y, int size) {
                        app.a.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
                    }

                    @Override
                    public void click(UITabBar.Tab self) {
                        tabPane.removeTab(self);
                        // Since this was manually removed, this must be called manually.
                        uie.onWindowClose();
                    }
                });
            }
            tabPane.addTab(new UITabBar.Tab(uie, icons.toArray(new UITabBar.TabIcon[0])));
            tabPane.selectTab(uie);
        } else {
            if (app.c.windowingExternal && !GaBIEn.singleWindowApp()) {
                UIWindowView uwv = new UIWindowView() {
                    @Override
                    public void onWindowClose() {
                        allWindowViews.remove(this);
                        createWindow(uie, true, immortal);
                    }

                    @Override
                    public String toString() {
                        return uie.toString();
                    }
                };
                allWindowViews.add(uwv);
                uwv.setForcedBounds(null, new Rect(uie.getSize()));
                uwv.addShell(new UIWindowView.ScreenShell(uwv, uie));
                uiTicker.accept(uwv, false);
            } else {
                icons.addFirst(new UITabBar.TabIcon() {
                    @Override
                    public void draw(IGrDriver igd, int x, int y, int size) {
                        Art.tabWindowIcon(igd, x, y, size);
                    }

                    @Override
                    public void click(UITabBar.Tab tab) {
                        rootView.removeTab(tab);
                        createWindow(uie, true, immortal);
                    }
                });
                if (!immortal) {
                    icons.addFirst(new UITabBar.TabIcon() {
                        @Override
                        public void draw(IGrDriver igd, int x, int y, int size) {
                            app.a.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
                        }

                        @Override
                        public void click(UITabBar.Tab tab) {
                            rootView.removeTab(tab);
                            // We are actually closing (this isn't called by default for a remotely triggered remove)
                            uie.onWindowClose();
                        }
                    });
                }

                // Disposition logic
                Rect r = null;
                if (disposition != null)
                    r = recordedWindowPositions.get(disposition);

                // Make the shell, then apply disposition override
                // This is because TabShell sanitizes the size on init
                TabShell tabShell = new UIWindowView.TabShell(rootView, uie, icons.toArray(new UITabBar.TabIcon[0])) {
                    @Override
                    public void windowBoundsCheck() {
                        super.windowBoundsCheck();
                        if (disposition != null)
                            recordedWindowPositions.put(disposition, contents.getParentRelativeBounds());
                    }
                };
                if (r != null)
                    uie.setForcedBounds(null, r);
                rootView.addShell(tabShell);
            }
        }
    }

    public void createMenu(UIElement base, UIElement menu) {
        UIElement trueBase = base;
        // This function is evil!
        int baseScreenX = 0;
        int baseScreenY = 0;
        Size baseSize = base.getSize();

        while (true) {
            Rect r = base.getParentRelativeBounds();
            baseScreenX += r.x;
            baseScreenY += r.y;
            UIElement uie = base.getParent();
            if (uie == null)
                break;
            base = uie;
        }

        for (UIWindowView uwv : allWindowViews) {
            for (UIWindowView.IShell shl : uwv.getShells()) {
                if (shl instanceof UIWindowView.TabShell) {
                    if (((UIWindowView.TabShell) shl).contents == base) {
                        createMenuCore(uwv, trueBase, new Rect(baseScreenX, baseScreenY, baseSize.width, baseSize.height), menu);
                        return;
                    }
                } else if (shl instanceof UIWindowView.ElementShell) {
                    if (((UIWindowView.ElementShell) shl).uie == base) {
                        createMenuCore(uwv, trueBase, new Rect(baseScreenX, baseScreenY, baseSize.width, baseSize.height), menu);
                        return;
                    }
                }
            }
        }
        System.err.println("WindowManager failed to find desktop to deploy context menu.");
        createWindow(menu);
    }

    private void createMenuCore(final UIWindowView screen, final UIElement baseElem, final Rect base, final UIElement menu) {
        Size sz = menu.getSize();
        Rect area = new Rect(screen.getSize());
        int sanityBorder = Math.min(area.width, area.height) / 8;
        if (sanityBorder < 1)
            sanityBorder = 1;
        Rect[] results = new Rect[] {
                new Rect(base.x, base.y + base.height, sz.width, sz.height),
                new Rect((base.x + base.width) - sz.width, base.y + base.height, sz.width, sz.height),
                new Rect(base.x, base.y - sz.height, sz.width, sz.height),
                new Rect((base.x + base.width) - sz.width, base.y - sz.height, sz.width, sz.height),
                new Rect((area.width - sz.width) / 2, (area.width - sz.width) / 2, sz.width, sz.height),
                // There's simply not enough room for the menu in any sane configuration.
                // Now, the ability to cancel the menu still needs to be maintained, so there's a border here.
                new Rect(sanityBorder, sanityBorder, area.width - (sanityBorder * 2), area.height - (sanityBorder * 2)),
        };
        for (int i = 0; i < results.length; i++) {
            Rect r2 = results[i].getIntersection(area);
            if (r2 != null) {
                if (r2.rectEquals(results[i])) {
                    menu.setForcedBounds(null, r2);
                    screen.addShell(new UIWindowView.ElementShell(screen, menu) {
                        @Override
                        public IPointerReceiver provideReceiver(IPointer i) {
                            IPointerReceiver ipr = super.provideReceiver(i);
                            if (ipr == null) {
                                screen.removeShell(this);
                                uie.onWindowClose();
                                return IPointerReceiver.NopPointerReceiver.I;
                            }
                            return ipr;
                        }

                        @Override
                        public void render(IGrDriver igd) {
                            Size sz = screen.getSize();
                            igd.blitScaledImage(0, 0, 1, 1, 0, 0, sz.width, sz.height, modImg);
                            int bw = 4;
                            Rect r = menu.getParentRelativeBounds();
                            Theme theme = GaBIEnUI.sysThemeRoot.getTheme();
                            // The border is shown 'behind' the menu base, but the menu is shown over it
                            UIBorderedElement.drawBorder(theme, igd, Theme.B_R48OVERLAY, bw, r.x - bw, r.y - bw, r.width + (bw * 2), r.height + (bw * 2));
                            float otx = igd.trsTXS(base.x);
                            float oty = igd.trsTYS(base.y);
                            baseElem.renderAllLayers(igd);
                            igd.trsTXYE(otx, oty);
                            super.render(igd);
                        }
                    });
                    return;
                }
            }
        }
        System.err.println("WindowManager failed to deploy context menu despite all fallbacks.");
        createWindow(menu);
    }

    public Size getRootSize() {
        return rootView.getSize();
    }

    public void setOrange(double v) {
        tabPane.visualizationOrange = v;
    }

    public void debugDumpUITree(DatumWriter dw) {
        rootView.debugDumpUITree(dw);
    }
}
