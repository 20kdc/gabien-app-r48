/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests.grand;

import org.junit.Test;

/**
 * Created on March 28, 2019.
 */
public class GrandSynthesisTest {
    @Test
    public void runGrandSynthesisTest() {
        GrandTestBuilder gtb = new GrandTestBuilder();
        GrandInitializers.initSynthesis2k3(gtb);
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

    private void editTerm(GrandTestBuilder gtb) {
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
        gtb.thenClick("label::.annXEncounter ¥..¥textbox");
        gtb.thenType(" said hello!");
        gtb.thenIcon("RPG_RT.ldb", 0);
    }

    private void editMap(GrandTestBuilder gtb) {
        // Work on the map now.
        gtb.thenSelectTab("Map");
        gtb.thenWaitFrame();
        gtb.thenClick("?:r48.map.UIMapView", 32, 96); // Disable camera mode.
        gtb.thenWaitFrame();

        editMapTile(gtb);
        editMapEvent(gtb);
    }

    public void editMapTile(GrandTestBuilder gtb) {
        gtb.thenClick("button:L0"); // L0
        gtb.thenWaitFrame();
        gtb.thenIcon("T0", 1);
        gtb.thenClick("?: ATF", 35, 350); // AT-General Field 0
        gtb.thenSelectTab("Map");
        gtb.thenClick("?:r48.map.UIMapView", 336, 146); // Place tile @ 1, 1
    }

    public void editMapEvent(GrandTestBuilder gtb) {
        // Launch event editor, add an event, set the direction to 'down', do more stuff, and stop editing it
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
        editMapEventGraphics(gtb);
        editMapEventMoveRoute(gtb);
        gtb.thenIcon("Map0001.lmu*", 0); // Close.
    }

    private void editMapEventGraphics(GrandTestBuilder gtb) {
        gtb.thenClick("button:Graphics"); // Graphics
        gtb.thenWaitFrame();
        gtb.thenClick("button:2 : down"); // Direction
        gtb.thenWaitFrame();
        gtb.thenClick("button:2 : down"); // Down
        gtb.thenWaitFrame();
        gtb.thenClick("?:Map0001.lmu*¥symbol:Back"); // Back (to leave Graphics)
        gtb.thenWaitFrame();
    }

    private void editMapEventMoveRoute(GrandTestBuilder gtb) {
        gtb.thenClick("button:0 : don't"); // Setup move type
        gtb.thenWaitFrame();
        gtb.thenClick("button:6 : custom"); // Finish that
        gtb.thenWaitFrame();
        gtb.thenClick("button:RPG::MoveRoute"); // Into moveroute
        gtb.thenWaitFrame();
        gtb.thenClick("button:Route..."); // Continue...
        gtb.thenWaitFrame();
        gtb.thenClick("button:Add..."); // Continued...
        gtb.thenWaitFrame();
        gtb.thenClick("button:Insert Here..."); // Continued...
        gtb.thenWaitFrame();

        gtb.thenClick("button:11;Move Forward 1 Tile"); // Set command type
        gtb.thenWaitFrame();
        gtb.thenClick("?:Map0001.lmu*¥symbol:Back"); // Back (to leave the command)
        gtb.thenWaitFrame();

        gtb.thenClick("button:11 Move Forward 1 Tile¥..¥..¥button:Add..."); // New command...
        gtb.thenWaitFrame();
        gtb.thenClick("button:Add Next..."); // Continued...
        gtb.thenWaitFrame();

        gtb.thenScroll("?:Map0001.lmu*¥?:gabien.ui.UITabPane¥scroll", "button:34;Set Graphic");
        gtb.thenClick("button:34;Set Graphic"); // Set command type
        gtb.thenWaitFrame();
        gtb.thenClick("button:Select character index...");
        gtb.thenWaitFrame();
        gtb.thenClick("?:r48.ui.dialog.UISpritesheetChoice¥?:gabien.ui.UIScrollbar¥..", 128, 0);
        gtb.thenWaitFrame();
        gtb.thenClick("?:Map0001.lmu*¥symbol:Back"); // Back (to leave the spritesheet chooser)
        gtb.thenClick("?:Map0001.lmu*¥symbol:Back"); // Back (to leave the command)
        gtb.thenClick("?:Map0001.lmu*¥symbol:Back"); // Back (to leave the command list)
        gtb.thenClick("?:Map0001.lmu*¥symbol:Back"); // Back (to leave the moveroute)
        gtb.thenWaitFrame();
    }

    private void editNewMap(GrandTestBuilder gtb) {
        gtb.thenSelectTab("MapInfos");
        gtb.thenClick("button:<Insert New Map>");
        gtb.thenIcon("Map ID?", 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:Confirm");
        gtb.thenWaitFrame();
    }

    private void runTheTest(GrandTestBuilder gtb) {
        gtb.thenWaitFrame();
        gtb.execute(3561671);
    }
}
