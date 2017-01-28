/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.musicality;

import gabien.GaBIEn;
import gabien.ui.ISupplier;

/**
 * Created on 1/25/17.
 */
public class Musicality {

    private static double collectedTime = 0;
    private static Instrument primaryTrack, couplingTrack, secondaryTrack;
    private static ISupplier<Integer> primaryTrackController;
    private static int metatick = 0;
    public static boolean initialized = false;
    public static boolean running = false;

    public static void initialize() {
        primaryTrack = new Instrument(GaBIEn.getSound().createChannel());
        couplingTrack = new Instrument(GaBIEn.getSound().createChannel());
        secondaryTrack = new Instrument(GaBIEn.getSound().createChannel());
        secondaryTrack.mul = 1;
        secondaryTrack.div = 5;
        primaryTrackController = null;
        running = false;
        initialized = true;
    }

    private static void tick() {
        primaryTrack.update();
        couplingTrack.update();
        secondaryTrack.update();
        metatick++;
        if (metatick == 38) {
            metatick = 0;
            performMetatick();
        }
    }

    private static void performMetatick() {
        int r = primaryTrackController.get();
        primaryTrack.startNote(r);
        couplingTrack.startNote(r * 2);
    }

    public static void update(double dT) {
        collectedTime += dT;
        while (collectedTime > 0.02d) {
            tick();
            collectedTime -= 0.02d;
        }
    }

    public static void boot() {
        primaryTrackController = new PrimaryTrackController(secondaryTrack);
        running = true;
    }

    public static void kill() {
        primaryTrack.kill();
        couplingTrack.kill();
        secondaryTrack.kill();
        running = false;
    }
}
