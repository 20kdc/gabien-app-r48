/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSrcLoc;
import datum.DatumSymbol;
import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabienapp.state.LSMain;
import gabienapp.state.LSSplashScreen;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.minivm.MVMEnv;
import r48.tr.DynTrBase;
import r48.tr.IDynTrProxy;
import r48.tr.LanguageList;
import r48.ui.Art;

/**
 * Rethink of how this should work for code reasons.
 * Created 27th February 2023.
 */
public class Launcher {
    public final boolean isMobile;
    public final WindowCreatingUIElementConsumer uiTicker;
    public State currentState;

    // Warning: These are not finished until during splash screen (and off-thread at that)
    public volatile Config c;
    public volatile MVMEnv vmCtx;
    public volatile InterlaunchGlobals ilg;

    public int globalMS = 50;
    private double compensationDT;

    public Launcher(boolean strict) {
        isMobile = GaBIEn.singleWindowApp();
        final AtomicBoolean configLoaded = new AtomicBoolean();
        uiTicker = new WindowCreatingUIElementConsumer();
        // Setup initial state
        currentState = new LSSplashScreen(this, () -> {
            // prewarm MXbean
            GaBIEn.getLoadedClassCount();
            int loadedClassesStart = GaBIEn.getLoadedClassCount();
            // -- start --
            // Initialize as much as possible here.
            c = new Config(isMobile);
            configLoaded.set(ConfigIO.load(true, c));
            ilg = new InterlaunchGlobals(new Art(), c, (vm) -> vmCtx = vm, (str) -> {
                // this would presumably go to the splash screen
            }, (str) -> System.err.println("TR: " + str), strict);
            try {
                Class.forName("gabienapp.CriticalClassLoading").getMethod("actuallyLoad", InterlaunchGlobals.class).invoke(null, ilg);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            boolean canAvoidWait = c.fontOverride == null;
            // If we're setup correctly: English never needs the font-loading.
            // The reason it's important we use the correct language for this is because if font-loading is slow,
            //  we WILL (not may, WILL) freeze up until ready.
            boolean fontsNecessary = true;
            if (canAvoidWait)
                if (c.language.equals(LanguageList.hardcodedLang))
                    fontsNecessary = false;
            if (fontsNecessary)
                while (!GaBIEn.fontsReady)
                    Thread.yield();
            // -- end --
            int loadedClassesEnd = GaBIEn.getLoadedClassCount();
            if (loadedClassesStart != -1) {
                System.err.println("Launcher: Loaded " + (loadedClassesEnd - loadedClassesStart) + " classes.");
            } else {
                System.err.println("Launcher: Unable to measure classes loaded.");
            }
        }, (uiScaleTenths) -> {
            c.applyUIGlobals();
            c.autodetectedUIScaleTenths = uiScaleTenths;
            globalMS = 33;
            if (!configLoaded.get())
                c.resetFontSizes(); // will fixup UI globals itself
            currentState = new LSMain(this);
        });
    }

    public void run() {
        // Note the mass-recreate.
        while (currentState != null) {
            double dTTarg = (globalMS / 1000d) - compensationDT;
            double dT = GaBIEn.endFrame(dTTarg);
            compensationDT = Math.min(dTTarg, dT - dTTarg);
            currentState.tick(dT);
        }
        GaBIEn.ensureQuit();
    }

    void shutdownAllAppMainWindows() {
        for (UIElement uie : uiTicker.runningWindows())
            uiTicker.forceRemove(uie);
    }

    public abstract static class State implements IDynTrProxy {
        public final Launcher lun;
        public final Config c;
        public State(Launcher lun) {
            this.lun = lun;
            this.c = lun.c;
        }

        public abstract void tick(double dT);

        @Override
        public DynTrBase dynTrBase(DatumSrcLoc srcLoc, String id, @Nullable DatumSymbol mode, Object text, boolean isNLS) {
            return lun.ilg.dynTrBase(srcLoc, id, mode, text, isNLS);
        }
    }
}
