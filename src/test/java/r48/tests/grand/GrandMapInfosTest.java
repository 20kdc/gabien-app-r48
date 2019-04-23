/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests.grand;

import org.junit.Test;

/**
 * Created on April 21, 2019.
 */
public class GrandMapInfosTest {
    @Test
    public void testMIR2k3() {
        GrandTestBuilder gtb = new GrandTestBuilder();
        GrandInitializers.initSynthesis2k3(gtb);
        gtb.thenSelectTab("MapInfos");
        gtb.thenWaitFrame();
        gtb.thenClick("button:1:First Map P0");
        gtb.thenWaitFrame();
        gtb.thenClick("button:Delete");
        gtb.thenWaitFrame();
        gtb.thenClick("button:Confirm");
        gtb.thenWaitFrame();
        performMapSystemTest(gtb);
        gtb.thenClick("symbol:Save");
        gtb.thenCloseWindow();
        gtb.thenCloseWindow();
        gtb.execute(3581586);
    }

    @Test
    public void testMIRXP() {
        GrandTestBuilder gtb = new GrandTestBuilder();
        GrandInitializers.initSynthesisRXP(gtb);
        performMapSystemTest(gtb);
        gtb.thenClick("symbol:Save");
        gtb.thenCloseWindow();
        gtb.thenCloseWindow();
        gtb.execute(282280);
    }

    private void performMapSystemTest(GrandTestBuilder gtb) {
        gtb.thenSelectTab("MapInfos");
        performInsertNewMap(gtb);
        performInsertNewMap(gtb);
        performInsertNewMap(gtb);
        performMoveOut(gtb, "2: P1");
        performMoveOut(gtb, "3: P2");
        performMoveTo(gtb, "3: P0", "1: P0");
        performMoveTo(gtb, "2: P0", "1: P0");
    }

    private void performInsertNewMap(GrandTestBuilder gtb) {
        gtb.thenClick("button:<Insert New Map>");
        gtb.thenIcon("Map ID?", 1);
        gtb.thenWaitFrame();
        gtb.thenClick("button:Confirm");
        gtb.thenWaitFrame();
        gtb.thenSelectTab("MapInfos");
    }

    private void performMoveOut(GrandTestBuilder gtb, String p0_) {
        gtb.thenClick("button:" + p0_);
        gtb.thenWaitFrame();
        gtb.thenClick("button:Move Out ");
        gtb.thenWaitFrame();
    }

    private void performMoveTo(GrandTestBuilder gtb, String s, String s1) {
        gtb.thenDrag("button:" + s, -4, 16, "button:" + s1, -4, 0);
        gtb.thenWaitFrame();
        gtb.thenWaitFrame();
    }
}

