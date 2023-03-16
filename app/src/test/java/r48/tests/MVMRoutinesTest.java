/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gabien.TestKickstart;
import r48.minivm.MVMEnvR48;
import r48.minivm.fn.MVMR48GlobalLibraries;

/**
 * Created 16th March 2023 to test extra stuff.
 */
public class MVMRoutinesTest {
    @Test
    public void testAnd() {
        new TestKickstart().kickstartRFS();
        MVMEnvR48 env = new MVMEnvR48((s) -> {}, (s) -> {}, "tok");
        MVMR48GlobalLibraries.add(env);
        env.include("vm/global", false);

        assertEquals(3L, env.evalString("(and 1 2 3)"));
        assertEquals(false, env.evalString("(and 1 #f 3)"));
        assertEquals(true, env.evalString("(and)"));
        assertEquals(1L, env.evalString("(and 1)"));
    }

    @Test
    public void testOr() {
        new TestKickstart().kickstartRFS();
        MVMEnvR48 env = new MVMEnvR48((s) -> {}, (s) -> {}, "tok");
        MVMR48GlobalLibraries.add(env);
        env.include("vm/global", false);

        assertEquals(1L, env.evalString("(or 1 2 3)"));
        assertEquals(1L, env.evalString("(or 1 #f 3)"));
        assertEquals(false, env.evalString("(or)"));
        assertEquals(1L, env.evalString("(or 1)"));
    }

    @Test
    public void testCond() {
        new TestKickstart().kickstartRFS();
        MVMEnvR48 env = new MVMEnvR48((s) -> {}, (s) -> {}, "tok");
        MVMR48GlobalLibraries.add(env);
        env.include("vm/global", false);

        assertEquals(1L, env.evalString("(or 1 2 3)"));
        assertEquals(1L, env.evalString("(or 1 #f 3)"));
        assertEquals(false, env.evalString("(or)"));
        assertEquals(1L, env.evalString("(or 1)"));
    }
}
