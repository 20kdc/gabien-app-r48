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
        gtb.thenClick("symbol:Save");
        gtb.thenWaitFrame();
        gtb.thenCloseWindow();
        gtb.thenCloseWindow();
        runTheTest(gtb);
    }

    private void initSynthesis(GrandTestBuilder gtb) throws IOException {
        gtb.thenWaitWC(2);
        gtb.thenClick("button:RPG Maker 2000, 2003, or EasyRPG. (Android users, go here.)"); // R2k
        gtb.thenWaitFrame();
        gtb.thenClick("button:R2K[3?] (UTF-8) (Use for new games or languages not shown.) "); // Some encoding or another...
        gtb.thenWaitFrame();
        // Maximize 'Please confirm...'
        gtb.thenIcon(5, 6, 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:2003 Template");
        gtb.thenWaitFrame();
        // Close 'Information'.
        gtb.thenIcon("Information", 0);
    }

    private void editTerm(GrandTestBuilder gtb) throws IOException {
        // Show + Maximize 'RPG_RT.ldb', scroll down, edit first term, and close it
        gtb.thenSelectTab("Database Objects");
        gtb.thenWaitFrame();
        gtb.thenClick("button:RPG_RT.ldb");
        gtb.thenWaitFrame();
        gtb.thenIcon("RPG_RT.ldb", 1);
        gtb.thenWaitFrame();
        gtb.thenScroll("?:RPG_RT.ldb¥scroll", "label:@terms ¥..¥button");
        gtb.thenWaitFrame();
        gtb.thenClick("label:@terms ¥..¥button");
        gtb.thenWaitFrame();
        gtb.thenClick("label:annXEncounter¥..¥textbox");
        gtb.thenType(" said hello!");
        gtb.thenIcon("RPG_RT.ldb", 0);
    }

    private void editMap(GrandTestBuilder gtb) throws IOException {
        // Work on the map now.
        gtb.thenSelectTab("Map");
        gtb.thenWaitFrame();
        gtb.thenClick("?:r48.map.UIMapView", 32, 96); // Disable camera mode.
        gtb.thenWaitFrame();
        gtb.thenClick("button:L0"); // L0
        gtb.thenWaitFrame();
        gtb.thenIcon("T0", 1);
        gtb.thenClick("?: ATF", 35, 350); // AT-General Field 0
        gtb.thenSelectTab("Map");
        gtb.thenClick("?:r48.map.UIMapView", 336, 146); // Place tile @ 1, 1
        // Launch event editor, add an event, set the direction to 'down', and stop editing it
        gtb.thenClick("button:Events"); // Events
        gtb.thenWaitFrame();
        gtb.thenIcon("Ev.Pick [0 total]", 1); // Move Ev picker into tab,
        gtb.thenSelectTab("Map"); // then go back to map, to ensure the screen is clear
        gtb.thenClick("?:r48.map.UIMapView", 336, 146); // Target point for event @ 1, 1
        gtb.thenWaitFrame();
        gtb.thenSelectTab("Ev.Pick [0 total]");
        gtb.thenClick("button:+ Add Event"); // Add Event
        gtb.thenIcon("Ev.Pick [0 total]", 0);
        gtb.thenIcon("Map0001.lmu*", 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:Graphics"); // Graphics
        gtb.thenWaitFrame();
        gtb.thenClick("button:2 : down"); // Direction
        gtb.thenWaitFrame();
        gtb.thenClick("button:2 : down"); // Down
        gtb.thenIcon("Map0001.lmu*", 0); // Close.
    }

    private void editNewMap(GrandTestBuilder gtb) throws IOException {
        gtb.thenSelectTab("MapInfos");
        gtb.thenClick("button:<Insert New Map>");
        gtb.thenIcon("Map ID?", 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:Confirm");
        gtb.thenWaitFrame();
    }

    private void runTheTest(GrandTestBuilder gtb) throws IOException {
        gtb.thenWaitFrame();
        gtb.execute(4565975);
    }
}
