/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;

import java.util.Random;

/**
 * For once, a simple to describe class.
 * Created on 1/2/17.
 */
public class TimeWaster {
    private double moveTime = 16;
    private int iconPlanX = 0;
    private int iconPlanY = 0;
    private int points = 0;
    private Random madness = new Random();

    public void draw(IGrInDriver igd, int ox, int oy, double deltaTime, int sw, int sh) {
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
                doJump(igd.getMouseX() - ox, igd.getMouseY() - oy, sw, sh);
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
        Rect b = new Rect(ox + iconPlanX, oy + iconPlanY, 64, 64);
        if (type == 0)
            if (b.contains(igd.getMouseX(), igd.getMouseY())) {
                moveTime = 8;
                points++;
            }
        igd.blitImage(type * 64, 0, 64, 64, ox + iconPlanX, oy + iconPlanY, AppMain.noMap);
        if (points > 1) {
            UILabel.drawString(igd, ox, oy, "You have " + points + " absolutely worthless points.", false, FontSizes.timeWasterTextHeight);
            String[] pointMsgs = new String[] {
                    "Now, get back to work!",
                    "Seriously? What are you doing?",
                    "You are supposed to use the MapInfos tab to select or create a map.",
                    "Like, select 'SECRET RAM CLUB'. I hear they give free memory sticks.",
                    "But really, point (aha) your mouse in the general direction of MapInfos.",
                    "Fine. DON'T select a map. Just keep following the trail...",
                    "No-one would have believed, in the last years of the 19th century,",
                    " that human affairs were being watched, from the timeless worlds of space.",
                    "And one of the watchers is hiding out in a game.",
                    "Ok, yeah, figured you wouldn't believe that. Too SCP-like.",
                    "Just going to reset the points counter now, it's clearly encouraging you.",
            };
            if (points - 2 == pointMsgs.length)
                points = 2;
            // Any GitHub issues on this will be disregarded.
            UILabel.drawString(igd, ox, oy + FontSizes.timeWasterTextHeight, pointMsgs[points - 2], false, FontSizes.timeWasterTextHeight);
        }
    }

    // x/y is a position to stay away from.
    private void doJump(int x, int y, int w, int h) {
        // By default, make it impossible to reach.
        iconPlanX = -64;
        iconPlanY = -64;
        if (w < 64)
            return;
        if (h < 64)
            return;
        // make it highly unlikely it will hit the same position twice
        for (int tries = 0; tries < 64; tries++) {
            iconPlanX = madness.nextInt(w - 64);
            iconPlanY = madness.nextInt(h - 64);
            Rect r2 = new Rect(iconPlanX, iconPlanY, 64, 64);
            if (!r2.contains(x, y))
                return;
        }
        // Fall back to hiding it
        iconPlanX = -64;
        iconPlanY = -64;
    }

}
