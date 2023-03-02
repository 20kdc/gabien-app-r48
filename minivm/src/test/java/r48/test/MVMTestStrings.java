/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.test;

import static org.junit.Assert.*;

import org.junit.Test;

import r48.minivm.MVMEnv;
import r48.minivm.fn.MVMCoreLibraries;

/**
 * Created 2nd March 2023
 */
public class MVMTestStrings {
    public static MVMEnv prepEnv() {
        MVMEnv env = new MVMEnv();
        MVMCoreLibraries.add(env);
        return env;
    }

    @Test
    public void testRoundtrip() {
        MVMEnv env = prepEnv();
        assertEquals("Test string", env.evalString("(list->string (string->list \"Test string\"))"));
    }

    @Test
    public void testSubstring() {
        MVMEnv env = prepEnv();
        assertEquals("strin", env.evalString("(substring \"Test string\" 5 10)"));
    }
}
