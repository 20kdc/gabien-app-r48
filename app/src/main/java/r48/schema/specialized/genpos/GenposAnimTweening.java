/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;


/**
 * Created on October 10, 2018.
 */
public class GenposAnimTweening {
    public final IGenposAnim workspaceAnim;
    public final IGenposFrame workspaceFrame;

    public GenposAnimTweening(IGenposAnim anim, IGenposFrame frame) {
        workspaceAnim = anim;
        workspaceFrame = frame;
    }

    public IGenposTweeningManagement.KeyTrack getTrack(int cellNumber, int prop) {
        int frameCount = workspaceAnim.getFrameCount();
        double[] inputs = new double[frameCount];
        boolean[] results = new boolean[frameCount];
        boolean shouldFloor = false;
        int fidx = workspaceAnim.getFrameIdx();
        for (int i = 0; i < results.length; i++) {
            workspaceAnim.setFrameIdx(i);
            if (workspaceFrame.getCellCount() > cellNumber) {
                IGenposTweeningProp tp = workspaceFrame.getCellPropTweening(cellNumber, prop);
                shouldFloor |= tp.round();
                inputs[i] = tp.getValue();
                results[i] = true;
            }
        }
        workspaceAnim.setFrameIdx(fidx);
        boolean[] results2 = new boolean[results.length];
        for (int i = 0; i < results.length; i++) {
            if (!results[i])
                continue;
            for (int j = i + 1; j < results.length; j++) {
                System.arraycopy(results, 0, results2, 0, results.length);
                for (int k = i + 1; k <= j; k++)
                    results2[k] = false;
                double[] ninputs = tween(inputs, results2, shouldFloor);
                boolean fail = false;
                for (int k = 0; k < frameCount; k++) {
                    if (ninputs[k] != inputs[k]) {
                        fail = true;
                        break;
                    }
                }
                if (!fail)
                    System.arraycopy(results2, 0, results, 0, results.length);
            }
        }
        return new IGenposTweeningManagement.KeyTrack(results, inputs, shouldFloor);
    }

    private double[] tween(double[] inputs, boolean[] results, boolean shouldFloor) {
        double[] ip = new double[inputs.length];
        int encountered = -1;
        for (int i = 0; i < inputs.length; i++) {
            if (results[i]) {
                ip[i] = inputs[i];
                if (encountered == -1) {
                    for (int j = 0; j < i; j++)
                        ip[j] = inputs[i];
                } else {
                    interpolate(ip, encountered, i, shouldFloor);
                }
                encountered = i;
            }
        }
        if (encountered != -1)
            for (int i = encountered + 1; i < inputs.length; i++)
                ip[i] = inputs[encountered];
        return ip;
    }

    private void interpolate(double[] inputs, int a, int b, boolean shouldFloor) {
        double vA = inputs[a];
        double vB = inputs[b];
        double vS = (vB - vA) / (b - a);
        for (int i = a + 1; i < b; i++) {
            int sf = i - a;
            inputs[i] = vA + (vS * sf);
            if (shouldFloor)
                inputs[i] = Math.floor(inputs[i]);
        }
    }

    public void disablePropertyKey(int frameIdx, int cellNumber, int prop, IGenposTweeningManagement.KeyTrack track) {
        track.track[frameIdx] = false;
        track.values = tween(track.values, track.track, track.shouldFloor);
        int fidx = workspaceAnim.getFrameIdx();
        for (int i = 0; i < track.track.length; i++) {
            workspaceAnim.setFrameIdx(i);
            if (workspaceFrame.getCellCount() > cellNumber) {
                IGenposTweeningProp tp = workspaceFrame.getCellPropTweening(cellNumber, prop);
                tp.setValue(track.values[i]);
                workspaceAnim.modifiedFrames();
            }
        }
        workspaceAnim.setFrameIdx(fidx);
    }
}
