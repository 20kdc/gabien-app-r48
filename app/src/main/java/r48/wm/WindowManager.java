/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.wm;

import gabien.*;
import gabien.natives.BadGPUEnum;
import gabien.render.IGrDriver;
import gabien.ui.*;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UITabPane;
import gabien.ui.layouts.UIWindowView;
import gabien.ui.layouts.UIWindowView.TabShell;
import gabien.uslx.append.Block;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import gabien.wsi.IDesktopPeripherals;
import gabien.wsi.IPeripherals;
import gabien.wsi.IPointer;
import r48.app.InterlaunchGlobals;
import r48.ui.Art;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import datum.DatumWriter;

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
    private Rect preFullscreenRect = null;
    // 'protected' elements here are accessible to the testing framework.
    private final UIWindowView rootView;
    protected final UITabPane tabPane;
    private final TrackedUITicker uiTicker;
    // It is required by the test system that all UIWindowViews be roots of their own windows.
    protected final LinkedList<UIWindowView> allWindowViews = new LinkedList<UIWindowView>();
    private boolean performingScreenTransfer = true;
    public final HashMap<String, Rect> recordedWindowPositions = new HashMap<String, Rect>();
    public final Consumer<IDesktopPeripherals> coco;
    public final InterlaunchGlobals ilg;

    public WindowManager(InterlaunchGlobals ilg, final Consumer<IDesktopPeripherals> coco, final WindowCreatingUIElementConsumer uiTick, UIElement thbrL, UIElement thbrR, IQuickStatusGetter qsg) {
        this.ilg = ilg;
        this.coco = coco;
        uiTicker = new TrackedUITicker(uiTick);
        rootView = new UIWindowView() {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
                if (peripherals instanceof IDesktopPeripherals)
                    coco.accept((IDesktopPeripherals) peripherals);
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
        rootView.windowTextHeight = ilg.c.f.windowFrameH;
        rootView.sizerVisual = rootView.windowTextHeight / 2;
        rootView.sizerActual = rootView.windowTextHeight;
        rootView.setForcedBounds(null, new Rect(0, 0, ilg.c.f.scaleGuess(800), ilg.c.f.scaleGuess(600)));

        tabPane = new UITabPane(ilg.c.f.tabTH, true, true, ilg.c.f.maintabsS, thbrL, thbrR) {
            float orange = 0.0f;
            double orangeLastUpdate = GaBIEn.getTime();
            double noiseTime = 0.0d;

            @Override
            public IPointerReceiver handleNewPointer(IPointer state) {
                IPointerReceiver res = super.handleNewPointer(state);
                if (selectedTab == null)
                    if (res == null)
                        System.gc();
                return res;
            }

            @Override
            public void renderNoTabPanel(IGrDriver igd, int x, int y, int w, int h) {
                int sz = Math.max(w, h);
                int ex = x - (sz - w) / 2;
                int ey = y - (sz - h) / 2;

                try (Block b = igd.openTRS(ex, ey, sz, sz)) {
                    // ok, actual rendering time, 0-1 let's go
                    double instantTime = GaBIEn.getTime();
                    double dT = instantTime - orangeLastUpdate;
                    float orangeSource = qsg.getOrange();
                    if (orange < orangeSource) {
                        orange += dT / 8.0d;
                        if (orange > orangeSource)
                            orange = orangeSource;
                    } else {
                        orange -= dT / 8.0d;
                        if (orange < orangeSource)
                            orange = orangeSource;
                    }
                    noiseTime += dT * (1.0f + (orange * 4.0f));
                    orangeLastUpdate = instantTime;
                    // --
                    float l0x = (float) (noiseTime * 1), l0y = (float) (noiseTime * -1);
                    float l1x = (float) (noiseTime * 0.2), l1y = (float) (noiseTime * -0.2);
                    float l2x = (float) (noiseTime * -0.5), l2y = (float) (noiseTime * -0.5);
                    float l3x = (float) (noiseTime * -0.3), l3y = (float) (noiseTime * 0.2);
                    float l4x = (float) (noiseTime * 0.4), l4y = (float) (noiseTime * 0.3);
                    int df = BadGPUEnum.DrawFlags.MagLinear | BadGPUEnum.DrawFlags.WrapS | BadGPUEnum.DrawFlags.WrapT;
                    int dnf = BadGPUEnum.DrawFlags.WrapS | BadGPUEnum.DrawFlags.WrapT;
                    igd.drawScaledColoured(l0x, l0y, 8192, 8192, 0, 0, 1, 1, ilg.a.gNoise, IGrDriver.BLEND_NONE, dnf, 0.125f, 0.125f, 0.25f, 1);
                    igd.drawScaledColoured(128 + l1x, 128 + l1y, 128, 128, 0, 0, 1, 1, ilg.a.gNoise, IGrDriver.BLEND_ADD, df, 0.125f + (orange * 0.25f), 0.125f + (orange * 0.25f), 0.25f, 1);
                    igd.drawScaledColoured(256 + l2x, 128 + l2y, 64, 64, 0, 0, 1, 1, ilg.a.gNoise, IGrDriver.BLEND_ADD, df, 0.125f + (orange * 0.25f), 0.25f + (orange * 0.25f), 0.25f, 1);
                    igd.drawScaledColoured(128 + l3x, 256 + l3y, 32, 32, 0, 0, 1, 1, ilg.a.gNoise, IGrDriver.BLEND_ADD, df, 0.125f + (orange * 0.25f), 0.25f + (orange * 0.25f), 0.25f, 1);
                    igd.drawScaledColoured(256 + l4x, 256 + l4y, 16, 16, 0, 0, 1, 1, ilg.a.gNoise, IGrDriver.BLEND_ADD, df, 0.125f + (orange * 0.75f), 0.125f + (orange * 0.25f), 0.125f, 1);
                    int control = ((int) (Math.sin(System.currentTimeMillis() / 13750987.08314d) * 64) + 128);
                    igd.fillRect(32, 32, 32, control, 0, 0, 1, 1);
                }

                int th = ilg.c.f.backgroundObjectMonitorTH;
                int m = th / 2;
                int ty = y + m;
                for (String s : qsg.getQuickStatus()) {
                    GaBIEn.engineFonts.drawString(igd, x + m, ty, s, false, false, th);
                    ty += th;
                }
            }
        };

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
                    ilg.a.drawSymbol(igd, Art.Symbol.CloneFrame, x, y, size, false, false);
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
                        ilg.a.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
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
            if (ilg.c.windowingExternal && !GaBIEn.singleWindowApp()) {
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
                            ilg.a.drawSymbol(igd, Art.Symbol.XRed, x, y, size, false, false);
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
                    screen.addShell(new UIWindowView.MenuShell(screen, menu, baseElem, base));
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

    public void debugDumpUITree(DatumWriter dw) {
        rootView.debugDumpUITree(dw);
    }
}
