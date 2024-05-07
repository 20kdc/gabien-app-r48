/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.ui.UIElement;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabienapp.state.LSMain;
import gabienapp.state.LSSplashScreen;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.cfg.ConfigIO;
import r48.cfg.FontSizes.FontSizeField;
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
        final AtomicBoolean fontsLoaded = new AtomicBoolean();
        uiTicker = new WindowCreatingUIElementConsumer();
        // Setup initial state
        currentState = new LSSplashScreen(this, () -> {
            // Initialize as much as possible here.
            c = new Config(isMobile);
            fontsLoaded.set(ConfigIO.load(true, c));
            ilg = new InterlaunchGlobals(new Art(), c, (vm) -> vmCtx = vm, (str) -> {
                // this would presumably go to the splash screen
            }, (str) -> System.err.println("TR: " + str), strict);
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
        }, (uiScaleTenths) -> {
            c.applyUIGlobals();
            globalMS = 33;
            if (!fontsLoaded.get())
                autoDetectCorrectUISize(uiScaleTenths);
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

    private void autoDetectCorrectUISize(int uiGuessScaleTenths) {
        // The above triggered a flush, which would cause the initial resize on SWPs.
        // That then allowed it to estimate a correct scale which ended up here.
        c.f.uiGuessScaleTenths = uiGuessScaleTenths;
        for (FontSizeField fsf : c.f.fields) {
            // as this is a touch device, map 8 to 16 (6 is for things that really matter)
            if (isMobile)
                if (fsf.get() == 8)
                    fsf.accept(16);
            // uiGuessScaleTenths was set manually.
            if (fsf != c.f.f_uiGuessScaleTenths)
                fsf.accept(c.f.scaleGuess(fsf.get()));
        }
        // exceptions
        if (isMobile)
            c.f.tilesTabTH *= 2;
        c.applyUIGlobals();
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
