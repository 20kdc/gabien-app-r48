/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp.state;

import java.util.concurrent.atomic.AtomicBoolean;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.WindowSpecs;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import gabien.uslx.append.IConsumer;
import gabienapp.Launcher;
import gabienapp.Launcher.State;
import r48.ui.Art;

/**
 * Initial state of the launcher.
 * Created 27th February 2023.
 */
public class LSSplashScreen extends State {
    private int frames = -1; // Fadeout
    private int timer2 = 0; // Baton
    private String movement = " "; // the baton is 'thrown'
    private final IConsumer<Integer> done;
    private boolean completed = false;
    private final IGrInDriver gi;
    private final AtomicBoolean donePrimaryTask = new AtomicBoolean(false);

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
        Thread txdbThread = new Thread() {
            @Override
            public void run() {
                task.run();
                donePrimaryTask.set(true);
            }
        };
        txdbThread.start();
    }

    @Override
    public void tick(double dT) {
        if (completed)
            return;
        gi.flush(); // to kickstart w/h
        gi.clearAll(255, 255, 255);
        int sz = (Math.min(gi.getWidth(), gi.getHeight()) / 4) * 2;
        Rect ltPos = Art.r48ico;
        Rect ltPos2 = Art.r48ver;

        // note the swap on Y from - (sz / 2) because of the version
        Rect pos = new Rect((gi.getWidth() / 2) - (sz / 2), (gi.getHeight() / 2) - ((sz * 3) / 4), sz, sz);
        int fxRatio = ltPos2.width * ltPos2.height;
        int aspectMul = (ltPos2.height * fxRatio) / ltPos2.width;
        int szVHeight = (sz * aspectMul) / fxRatio;
        // this is where the "big version number" maths get changed to "little version number" maths
        Rect pos2 = new Rect(pos.x + (sz / 4), pos.y + (pos.height + (pos.height / 16)), sz / 2, szVHeight / 2);
        gi.blitScaledImage(ltPos.x, ltPos.y, ltPos.width, ltPos.height, pos.x, pos.y, pos.width, pos.height, GaBIEn.getImageEx("layertab.png", false, true));
        int margin = sz / 124;
        gi.clearRect(192, 192, 192, pos2.x - (margin * 3), pos2.y - (margin * 3), pos2.width + (margin * 6), pos2.height + (margin * 6));
        gi.clearRect(128, 128, 128, pos2.x - (margin * 2), pos2.y - (margin * 2), pos2.width + (margin * 4), pos2.height + (margin * 4));
        gi.clearRect(0, 0, 0, pos2.x - margin, pos2.y - margin, pos2.width + (margin * 2), pos2.height + (margin * 2));
        gi.blitScaledImage(ltPos2.x, ltPos2.y, ltPos2.width, ltPos2.height, pos2.x, pos2.y, pos2.width, pos2.height, GaBIEn.getImageEx("layertab.png", false, true));


        // Can't translate for several reasons (but especially no fonts).
        // This is really the only reason any of the messages are likely to be seen.
        String waitingFor = null;
        // Doesn't matter if it switches font on the last frame or something, just make sure the application remains running
        if (!donePrimaryTask.get())
            waitingFor = "Loading";
        if (waitingFor == null) {
            frames++;
            if (frames == 16) {
                int r = gi.estimateUIScaleTenths();
                gi.shutdown();
                done.accept(r);
                completed = true;
                return;
            }
            waitingFor = "Fading";
        }
        char[] chars = {'|', '/', '-', '\\'};
        char ch = chars[timer2 % chars.length];
        timer2++;
        movement += "  ";
        // has to be internal-font-able, unless on Android
        int goodSize = 16;
        if (lun.isMobile)
            goodSize = gi.getHeight() / 32;
        if (goodSize < 8)
            goodSize = 8;
        int goodSizeActual = UILabel.getRecommendedTextSize("", goodSize).height;
        UILabel.drawLabel(gi, gi.getWidth(), 0, gi.getHeight() - goodSizeActual, waitingFor + movement + ch, 1, goodSize);

        // fade
        int c = Math.max(0, Math.min(255, 25 * frames)) << 24;
        gi.blitScaledImage(0, 0, 1, 1, 0, 0, gi.getWidth(), gi.getHeight(), GaBIEn.createImage(new int[] {c}, 1, 1));
    }

}