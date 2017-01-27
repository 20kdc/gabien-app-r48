/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.musicality;

import gabien.ui.ISupplier;

import java.util.Random;

/**
 * Not exactly the cleanest of code, but it gets the job done for now
 * Created on 1/25/17.
 */
public class PrimaryTrackController implements ISupplier<Integer> {

    private Instrument secondaryTrk;

    public PrimaryTrackController(Instrument secondaryTrack) {
        secondaryTrk = secondaryTrack;
        regeneratePattern();
    }

    private Random r = new Random();

    private int rn() {
        return r.nextInt(4) + 1;
    }

    private int[] seq, seq2, seq3;

    public int noteNb = 0;
    public int cycles = 0;
    private boolean v1 = false;
    private boolean v2 = false;
    private boolean v3 = false;
    private boolean speedupSecondaries = false;

    @Override
    public Integer get() {
        int nt = 7 * (speedupSecondaries ? 4 : 3);
        int[] targSeq = seq;
        if (v1) {
            targSeq = seq2;
            if (noteNb > 3)
                targSeq = seq3;
        }
        if ((noteNb & 3) == 0)
            if (speedupSecondaries)
                triggerNextSecondaryNote();
        nt += targSeq[noteNb & 3];
        if (noteNb > 7) {
            nt--;
            if (v1)
                v1 = false;
            if (!speedupSecondaries) {
                if (!v3) {
                    triggerNextSecondaryNote();
                    v3 = true;
                }
            }
        }
        noteNb++;
        if (noteNb == 16) {
            if (!speedupSecondaries)
                triggerNextSecondaryNote();
            v3 = false;
            noteNb = 0;
            if (!v2) {
                v1 = true;
                v2 = true;
            } else {
                v2 = false;
            }
            cycles++;
            if (cycles == 4) {
                cycles = 0;
                regeneratePattern();
                speedupSecondaries = !speedupSecondaries;
            }
        }
        return nt;
    }

    private void regeneratePattern() {
        seq = new int[] {
                rn(), rn(), rn(), rn()
        };
        seq2 = new int[] {
                seq[0], seq[1], seq[0], seq[1]
        };
        seq3 = new int[] {
                seq[0], seq[1], seq[0], seq[3]
        };
    }

    private void triggerNextSecondaryNote() {
        secondaryTrk.startNote(r.nextInt(56));
    }
}
