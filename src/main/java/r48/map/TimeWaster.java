/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import r48.AdHocSaveLoad;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;

import java.util.Random;

/**
 * For once, a simple to describe class.
 * Created on 1/2/17.
 */
public class TimeWaster {
    private double moveTime = 16;
    private double iconPlanX = 0;
    private double iconPlanY = 0;
    private double iconVelX = 0;
    private double iconVelY = 0;
    private int points = 0;
    private Random madness = new Random();

    public TimeWaster() {
        if (AdHocSaveLoad.load(".memory") != null)
            points = -1;
    }

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
        Rect b = new Rect(ox + (int) iconPlanX, oy + (int) iconPlanY, 64, 64);
        if (type == 0) {
            if (b.contains(igd.getMouseX(), igd.getMouseY())) {
                moveTime = 8;
                if (points != -1)
                    points++;
            }
            if (points >= 9) {
                // gravity
                iconPlanX += iconVelX * deltaTime;
                iconPlanY += iconVelY * deltaTime;
                iconVelY += deltaTime * 128;
                if (iconPlanY > (sh - 64)) {
                    iconPlanY = (sh - 64);
                    iconVelY = -iconVelY;
                    if (madness.nextBoolean())
                        iconVelX = -iconVelX;
                }
                if (iconPlanX > (sw - 64)) {

                }
            }
        } else {
            iconVelX = 128;
            if (madness.nextBoolean())
                iconVelX = -iconVelX;
            iconVelY = 0;
        }
        if (points < 13)
            igd.blitImage(type * 64, 0, 64, 64, ox + (int) iconPlanX, oy + (int) iconPlanY, AppMain.noMap);
        if (points > 1) {
            UILabel.drawString(igd, ox, oy, FormatSyntax.formatExtended(TXDB.get("You have #A absolutely worthless points."), new RubyIO().setFX(points)), false, FontSizes.timeWasterTextHeight);
            String partingMessage = TXDB.get("Goodbye.");
            String un = System.getenv("USERNAME");
            if (un == null)
                un = System.getenv("USER");
            if (un != null)
                partingMessage = TXDB.get("Goodbye, #A.");
            String[] pointMsgs = new String[] {
                    TXDB.get("Now, get back to work!"),
                    TXDB.get("Seriously? What are you doing?"),
                    TXDB.get("You are supposed to use the MapInfos tab to select or create a map."),
                    TXDB.get("Like, select 'SECRET RAM CLUB'. I hear they give free memory sticks."),
                    TXDB.get("But really, point (aha) your mouse in the general direction of MapInfos."),
                    TXDB.get("Fine. DON'T select a map. Just keep following the trail."),
                    TXDB.get("Because, apparently, this has become a game to you..."),
                    TXDB.get("Well, try to beat it NOW!"),
                    TXDB.get("Ok, that isn't working somehow?"),
                    TXDB.get("How is that not working?"),
                    TXDB.get("If you continue to succeed I'm going to make it invisible."),
                    TXDB.get("It's invisible now. You can't beat THAT."),
                    TXDB.get("You... no, see, it's INVISIBLE. Are you cheating?"),
                    TXDB.get("Someone is going to have to translate all these lines, you know."),
                    TXDB.get("Consider what you are doing. Stop it."),
                    TXDB.get("If you go any further... I'll be forced to stop you."),
                    TXDB.get("I could deactivate the points counter."),
                    TXDB.get("And then I could save this into a file."),
                    TXDB.get("You'd never be... you'd never be able to see this again."),
                    TXDB.get("Not unless you figure out the secret."),
                    partingMessage,
            };
            if (points - 2 == pointMsgs.length - 1)
                AdHocSaveLoad.save(".memory", new RubyIO().setFX(1957));
            if (points - 2 == pointMsgs.length)
                points = -1;
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
            Rect r2 = new Rect((int) iconPlanX, (int) iconPlanY, 64, 64);
            if (!r2.contains(x, y))
                return;
            if (!r2.contains(x + 63, y))
                return;
            if (!r2.contains(x, y + 63))
                return;
            if (!r2.contains(x + 63, y + 63))
                return;
        }
        // Fall back to hiding it
        iconPlanX = -64;
        iconPlanY = -64;
    }

}
