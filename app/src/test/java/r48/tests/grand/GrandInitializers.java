/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests.grand;

/**
 * Since this is the testing framework, adding 'wasteful' classes here has no negative effects!
 * Created on April 21, 2019.
 */
public class GrandInitializers {
    public static void initSynthesis2k3(GrandTestBuilder gtb) {
        gtb.thenWaitWC(2);
        gtb.thenClick("button:RPG Maker 2000, 2003, or EasyRPG. (Android users, go here.)"); // R2k
        gtb.thenWaitFrame();
        gtb.thenClick("button:R2K[3?] (UTF-8) (Use for new games or languages not shown.) "); // Some encoding or another...
        gtb.thenWaitWC(4);
        gtb.thenWaitFrame();
        // Maximize 'Please confirm...'
        gtb.thenIcon("Please confirm...", 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:2003 Template");
        gtb.thenWaitFrame();
        // Close 'Information'.
        gtb.thenIcon("Information", 0);
    }

    public static void initSynthesisRXP(GrandTestBuilder gtb) {
        gtb.thenWaitWC(2);
        gtb.thenClick("button:RGSS Engines (RPG Maker XP, VX Ace)");
        gtb.thenWaitFrame();
        gtb.thenClick("button:RXP");
        gtb.thenWaitWC(4);
        gtb.thenWaitFrame();
        gtb.thenIcon("gabien.ui.UIAutoclosingPopupMenu", 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:This appears to be newly created. Click to create directories.");
        gtb.thenWaitFrame();
        gtb.thenIcon("Information", 0);
        gtb.thenWaitFrame();
    }
}
