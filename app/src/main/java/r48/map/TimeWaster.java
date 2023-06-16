/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import r48.App;
import r48.ui.Art;

import java.util.Random;

import gabien.render.IGrDriver;

/**
 * For once, a simple to describe class.
 * Created on 1/2/17.
 */
public class TimeWaster {
    private double moveTime = 16;
    private int iconPlanX = 0;
    private int iconPlanY = 0;
    private final int iconSize;
    private Random madness = new Random();

    public TimeWaster(App app) {
        iconSize = 64 * app.f.getSpriteScale();
    }

    public void draw(IGrDriver igd, double deltaTime, int sw, int sh) {
        int stage = ((int) (moveTime / 8)) % 6;
        int type = 0;
        int mul = 1;
        int switchMul = 64;
        int darkMul = 4;
        int lightMul = 2;
        switch (stage) {
            case 0:
                type = 0;
                mul = lightMul;
                break;
            case 1:
                type = 1;
                mul = switchMul;
                break;
            case 2:
                type = 2;
                mul = darkMul;
                break;
            case 3:
                type = 2;
                // set random position
                doJump(sw, sh);
                mul = darkMul;
                break;
            case 4:
                type = 1;
                mul = switchMul;
                break;
            case 5:
                type = 0;
                mul = lightMul;
                break;
        }
        moveTime += deltaTime * mul;
        igd.blitScaledImage(0, 0, 64, 64, iconPlanX, iconPlanY, iconSize, iconSize, Art.noMap);
        type *= 127;
        if (type > 240)
            type = 255;
        igd.clearRectAlpha(0, 0, 0, type, iconPlanX, iconPlanY, iconSize, iconSize);
    }

    // x/y is a position to stay away from.
    private void doJump(int w, int h) {
        // By default, make it impossible to reach.
        iconPlanX = -iconSize;
        iconPlanY = -iconSize;
        if (w < iconSize)
            return;
        if (h < iconSize)
            return;
        iconPlanX = madness.nextInt(w - iconSize);
        iconPlanY = madness.nextInt(h - iconSize);
    }

}
