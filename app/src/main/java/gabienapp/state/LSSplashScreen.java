/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp.state;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import gabien.GaBIEn;
import gabien.pva.PVAFile;
import gabien.pva.PVARenderer;
import gabien.render.IGrDriver;
import gabien.text.RenderedTextChunk;
import gabien.text.TextTools;
import gabien.uslx.append.Rect;
import gabien.wsi.IGrInDriver;
import gabien.wsi.WindowSpecs;
import gabienapp.Launcher;
import gabienapp.PleaseFailBrutally;
import gabienapp.Launcher.State;

/**
 * Initial state of the launcher.
 * Created 27th February 2023.
 */
public class LSSplashScreen extends State {
    private int frames = -1; // Fadeout
    private double logoAnimationTimer = 0;
    private boolean completed = false, isFirstFrame = true;
    private final Consumer<Integer> done;
    private final IGrInDriver gi;
    private IGrDriver backBuffer;
    private final AtomicBoolean donePrimaryTask = new AtomicBoolean(false);
    private final AtomicReference<String> initError = new AtomicReference<>(null);
    private PVARenderer r48Logo;

    public LSSplashScreen(Launcher lun, Runnable task, Consumer<Integer> done) {
        super(lun);
        this.done = done;
        // Used for two reasons.
        // 1. to work out window size during a specific situation on Android.
        // 2. on certain distributions (Arch Linux), Java still freezes up during font load,
        //     so try to pave over it and PRETEND EVERYTHING'S FINE!!!
        //    I suspect network connectivity is involved, which is odd.
        WindowSpecs ws = GaBIEn.defaultWindowSpecs("R48 Startup...", 800, 600);
        ws.scale = 1;
        ws.resizable = true;
        gi = GaBIEn.makeGrIn("R48 Startup...", 800, 600, ws);
        // runs in parallel with font-load wait
        Thread txdbThread = new Thread(() -> {
            try {
                long a = System.currentTimeMillis();
                task.run();
                PleaseFailBrutally.checkFailBrutallyAtLoader();
                donePrimaryTask.set(true);
                long b = System.currentTimeMillis();
                System.err.println("R48: Splash: Spent " + (b - a) + "ms (of 1000ms logo time) on actual init.");
            } catch (Exception ex) {
                ex.printStackTrace();
                StringWriter sw = new StringWriter();
                // can't be translated; translator may have caused the error!
                sw.append("ERROR WHILE LOADING, WILL NOT CONTINUE\n\n");
                ex.printStackTrace(new PrintWriter(sw));
                initError.set(sw.toString());
            }
        });
        txdbThread.start();
    }

    @Override
    public void tick(double dT) {
        if (completed)
            return;

        if (!isFirstFrame) {
            logoAnimationTimer += dT * 1000;
        } else {
            PleaseFailBrutally.checkFailBrutallyAtSplash();
            System.err.println("R48: Splash: Took " + GaBIEn.getTime() + " seconds to get here");
            // This is kept separate from Art so that Art can be initialized off-thread
            try (InputStream inp = GaBIEn.getResource("animations/logo.pva")) {
                r48Logo = new PVARenderer(inp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            isFirstFrame = false;
        }
        backBuffer = gi.ensureBackBuffer(backBuffer);
        gi.flush(backBuffer); // to kickstart w/h
        IGrDriver bb = backBuffer = gi.ensureBackBuffer(backBuffer);
        bb.clearAll(255, 255, 255);

        int sz = (Math.min(bb.getWidth(), bb.getHeight()) / 4) * 2;

        String err = initError.get();

        // note the swap on Y from - (sz / 2) because of the version
        Rect pos = new Rect((bb.getWidth() / 2) - (sz / 2), (bb.getHeight() / 2) - (sz / 2), sz, sz);
        // this is where the "big version number" maths get changed to "little version number" maths
        if (r48Logo != null) {
            PVAFile.FrameElm[] logoFrame = r48Logo.pvaFile.frames[r48Logo.pvaFile.frameOfClamped(logoAnimationTimer)];
            int logoExpand = ((sz * r48Logo.pvaFile.header.width) / r48Logo.pvaFile.header.height) - sz;
            r48Logo.renderInline(logoFrame, bb, pos.x - (logoExpand / 2), pos.y, pos.width + logoExpand, pos.height);
        }

        if (err != null) {
            RenderedTextChunk rtc = TextTools.renderString(err, GaBIEn.getNativeFont(16, null, true), true);
            rtc.backgroundRoot(bb, 4, 4, 255, 255, 255, 255);
            rtc.renderRoot(bb, 4, 4);
        }

        frames++;

        if (donePrimaryTask.get()) {
            // 16 frames for WSI to stabilize OR 1 second
            if (frames >= 16 && logoAnimationTimer > 1000.0d) {
                int r = gi.estimateUIScaleTenths();
                gi.shutdown();
                done.accept(r);
                completed = true;
                return;
            }
        }
    }

}
