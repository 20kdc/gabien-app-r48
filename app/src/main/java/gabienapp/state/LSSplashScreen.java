/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp.state;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import gabien.GaBIEn;
import gabien.pva.PVAFile;
import gabien.pva.PVARenderer;
import gabien.render.IGrDriver;
import gabien.ui.Rect;
import gabien.uslx.append.IConsumer;
import gabien.wsi.IGrInDriver;
import gabien.wsi.WindowSpecs;
import gabienapp.Launcher;
import gabienapp.Launcher.State;

/**
 * Initial state of the launcher.
 * Created 27th February 2023.
 */
public class LSSplashScreen extends State {
    private int frames = -1; // Fadeout
    private double logoAnimationTimer = 0;
    private final IConsumer<Integer> done;
    private boolean completed = false, isFirstFrame = true;
    private final IGrInDriver gi;
    private IGrDriver backBuffer;
    private final AtomicBoolean donePrimaryTask = new AtomicBoolean(false);
    private PVARenderer r48Logo;

    public LSSplashScreen(Launcher lun, Runnable task, IConsumer<Integer> done) {
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
            long a = System.currentTimeMillis();
            task.run();
            donePrimaryTask.set(true);
            long b = System.currentTimeMillis();
            System.err.println("R48: Splash: Spent " + (b - a) + "ms (of 1000ms logo time) on actual init.");
        });
        txdbThread.start();
    }

    private static final Rect r48Ver = new Rect(33, 48, 31, 16);

    @Override
    public void tick(double dT) {
        if (completed)
            return;
        if (!isFirstFrame) {
            logoAnimationTimer += dT * 1000;
        } else {
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

        // note the swap on Y from - (sz / 2) because of the version
        Rect pos = new Rect((bb.getWidth() / 2) - (sz / 2), (bb.getHeight() / 2) - ((sz * 3) / 4), sz, sz);
        int fxRatio = r48Ver.width * r48Ver.height;
        int aspectMul = (r48Ver.height * fxRatio) / r48Ver.width;
        int szVHeight = (sz * aspectMul) / fxRatio;
        // this is where the "big version number" maths get changed to "little version number" maths
        Rect pos2 = new Rect(pos.x + (sz / 4), pos.y + (pos.height + (pos.height / 16)), sz / 2, szVHeight / 2);
        if (r48Logo != null) {
            PVAFile.FrameElm[] logoFrame = r48Logo.pvaFile.frames[r48Logo.pvaFile.frameOfClamped(logoAnimationTimer)];
            int logoExpand = ((sz * r48Logo.pvaFile.header.width) / r48Logo.pvaFile.header.height) - sz;
            r48Logo.renderInline(logoFrame, bb, pos.x - (logoExpand / 2), pos.y, pos.width + logoExpand, pos.height);
        }
        int margin = sz / 124;
        bb.clearRect(192, 192, 192, pos2.x - (margin * 3), pos2.y - (margin * 3), pos2.width + (margin * 6), pos2.height + (margin * 6));
        bb.clearRect(128, 128, 128, pos2.x - (margin * 2), pos2.y - (margin * 2), pos2.width + (margin * 4), pos2.height + (margin * 4));
        bb.clearRect(0, 0, 0, pos2.x - margin, pos2.y - margin, pos2.width + (margin * 2), pos2.height + (margin * 2));
        bb.blitScaledImage(r48Ver.x, r48Ver.y, r48Ver.width, r48Ver.height, pos2.x, pos2.y, pos2.width, pos2.height, GaBIEn.getImageEx("layertab.png", false, true));

        if (donePrimaryTask.get()) {
            frames++;
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
