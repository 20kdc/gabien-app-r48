/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.musicality;

import gabien.SimpleMixer;

/**
 * Used to keep developers content as they work.
 * As long as this has an off button, it can't harm anything.
 * Created on 1/25/17.
 */
public class Instrument {
    private SimpleMixer.Channel myChannel;
    private short[] data = new short[2205];
    private int adsrProgress = 0;
    public int mul = 2;
    public int div = 1;

    public static double[] noteO8Hertz = {
            4186.01,
            4698.63,
            5274.04,
            5587.65,
            6271.93,
            7040.00,
            7902.13
    };

    public static double getNOHertz(int octave, int note) {
        double res = noteO8Hertz[note];
        for (int i = 8; i > octave; i--)
            res /= 2;
        return res;
    }

    public static double getNoteHertz(int note) {
        return getNOHertz(note / 7, note % 7);
    }

    public Instrument(SimpleMixer.Channel channel) {
        for (int i = 0; i < 2205; i++)
            data[i] = (short) (Math.sin((i / 2205.0d) * 6.242d) * 16384);
        myChannel = channel;
    }

    // Runs 20 times a second.
    public void update() {
        int[] tbl = {
                1, 2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 7, 7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 0, 0, 0, 0
        };
        int idx = (adsrProgress * mul) / div;
        if (idx < tbl.length)
            myChannel.setVolume(tbl[idx] / 16.0d, tbl[idx] / 16.0d);
        adsrProgress++;
    }

    public void startNote(int nt) {
        // standard: 10hz
        myChannel.playSound(getNoteHertz(nt) / 10.0d, 0.0d, 0.0d, data, true);
        adsrProgress = 0;
    }

    public void kill() {
        myChannel.setVolume(0, 0);
    }
}
