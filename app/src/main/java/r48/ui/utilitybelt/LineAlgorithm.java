/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.uslx.append.*;

/**
 * The Totally Not A Derivative Of That Algorithm Meddling Kids And Their Dog Use For Line Drawing Algorithm.
 * Created on October 09, 2018.
 */
public class LineAlgorithm {
    public int ax, ay;

    public boolean run(int nx, int ny, IFunction<Boolean, Boolean> plotPointContinue) {

        int absX = Math.abs(ax - nx);
        int absY = Math.abs(ay - ny);

        int sub = absX;

        int subV = sub;
        int subS = absY;

        while ((absX > 0) || (absY > 0)) {
            if (!plotPointContinue.apply(false))
                return false;

            subV -= subS;
            boolean firstApp = true;
            while ((subV <= 0) && (absY > 0)) {
                if (!firstApp)
                    if (!plotPointContinue.apply(false))
                        return false;
                firstApp = false;
                // Move perpendicular
                if (ay < ny) {
                    ay++;
                    absY--;
                } else if (ay > ny) {
                    ay--;
                    absY--;
                }
                subV += sub;
            }
            // Move
            if (ax < nx) {
                ax++;
                absX--;
            } else if (ax > nx) {
                ax--;
                absX--;
            }
        }

        if ((ax != nx) || (ay != ny))
            System.out.println("Warning " + ax + "," + ay + ":" + nx + "," + ny);
        return plotPointContinue.apply(true);
    }
}
