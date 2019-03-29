/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests.grand;

import org.junit.Test;

import java.io.IOException;

/**
 * Created on March 28, 2019.
 */
public class GrandSynthesisTest {
    @Test
    public void runGrandSynthesisTest() throws IOException {
        GrandTestBuilder gtb = new GrandTestBuilder();
        initSynthesis(gtb);
        editTerm(gtb);
        editMap(gtb);
        editNewMap(gtb);

        // Save & quit
        gtb.thenClick(945, 15);
        gtb.thenWaitFrame();
        gtb.thenCloseWindow();
        gtb.thenCloseWindow();
        runTheTest(gtb);
    }

    private void initSynthesis(GrandTestBuilder gtb) {
        gtb.thenWaitWC(2);
        gtb.thenClick(358, 233);
        gtb.thenClick(333, 321);
        gtb.thenWaitFrame();
        // Maximize 'Please confirm...'
        gtb.thenIcon(5, 6, 1);
        gtb.thenClick(30, 526);
        gtb.thenWaitFrame();
        // Close 'Information'.
        gtb.thenIcon("Information", 0);
    }

    private void editTerm(GrandTestBuilder gtb) {
        // Show + Maximize 'RPG_RT.ldb', scroll down, edit first term, and close it
        gtb.thenSelectTab("Database Objects");
        gtb.thenClick(100, 82);
        gtb.thenWaitFrame();
        gtb.thenIcon("RPG_RT.ldb", 1);
        gtb.thenDrag(940, 86, 940, 523);
        gtb.thenClick(100, 278);
        gtb.thenClick(90, 79);
        gtb.thenType(" said hello!");
        gtb.thenIcon("RPG_RT.ldb", 0);
    }

    private void editMap(GrandTestBuilder gtb) {
        // Work on the map now.
        gtb.thenSelectTab("Map");
        gtb.thenClick(23, 177); // Disable camera mode.
        gtb.thenClick(14, 51); // L0
        gtb.thenWaitFrame();
        gtb.thenIcon("T0", 1);
        gtb.thenClick(35, 414); // AT-General Field 0
        gtb.thenSelectTab("Map");
        gtb.thenClick(336, 206); // Place tile
        // Launch event editor, add an event, set the direction to 'down', and stop editing it
        gtb.thenClick(78, 48); // Events
        gtb.thenWaitFrame();
        gtb.thenIcon("Ev.Pick [0 total]", 1); // Move Ev picker into tab,
        gtb.thenSelectTab("Map"); // then go back to map, to ensure the screen is clear
        gtb.thenClick(335, 208); // Target point for event
        gtb.thenWaitFrame();
        gtb.thenSelectTab("Ev.Pick [0 total]");
        gtb.thenClick(144, 51); // Add Event
        gtb.thenIcon("Ev.Pick [0 total]", 0);
        gtb.thenIcon("Map0001.lmu*", 1);
        gtb.thenClick(99, 271); // Graphics
        gtb.thenClick(62, 204); // Direction
        gtb.thenClick(224, 177); // Down
        gtb.thenIcon("Map0001.lmu*", 0); // Close.
    }

    private void editNewMap(GrandTestBuilder gtb) throws IOException {
        gtb.thenSelectTab("MapInfos");
        gtb.thenClick(288, 83);
        gtb.thenIcon("Map ID?", 1);
        gtb.thenClick(950, 50);
        gtb.thenWaitFrame();
    }

    private void runTheTest(GrandTestBuilder gtb) throws IOException {
        gtb.thenWaitFrame();
        gtb.execute(4565975);
    }
}
