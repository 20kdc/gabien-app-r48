/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import static gabien.datum.DatumTreeUtils.*;

import org.junit.Test;

import r48.minivm.MVMEnv;
import r48.minivm.fn.MVMCoreLibraries;
import r48.minivm.fn.MVMFn;

/**
 * Created 1st March
 */
public class MVMTests {
    private MVMEnv prepEnv() {
        MVMEnv env = new MVMEnv();
        MVMCoreLibraries.add(env);
        return env;
    }
    private void disasm(MVMEnv env, String p, String sym) {
        System.out.println(p + ": " + MVMFn.asUserReadableString(env.evalString("(mvm-disasm " + sym + ")")));
    }

    @Test
    public void testConstants() {
        MVMEnv env = prepEnv();
        assertEquals(1L, env.evalString("1"));
    }
    @Test
    public void testQuote() {
        // test quoting
        MVMEnv env = prepEnv();
        assertEquals(Arrays.asList(sym("example"), 1L), env.evalString("'(example 1)"));
    }

    @Test
    public void testTopLevelDefines() {
        MVMEnv env = prepEnv();
        assertEquals(Arrays.asList(sym("example"), 1L), env.evalString("(define testsym '(example 1))"));
        assertEquals(Arrays.asList(sym("example"), 1L), env.evalString("testsym"));
    }

    @Test
    public void testTopLevelDefinedFunctions() {
        MVMEnv env = prepEnv();
        env.evalString("(define (testsym a) a)");
        disasm(env, "testTopLevelDefinedFunctions", "testsym");
        assertEquals(Arrays.asList(sym("example"), 1L), env.evalString("(testsym '(example 1))"));
    }

    @Test
    public void testLambdas() {
        MVMEnv env = prepEnv();
        env.evalString("(define testsym (lambda () (define test 123) (lambda () test)))");
        disasm(env, "testLambdas", "testsym");
        assertEquals(123L, env.evalString("(testsym)"));
    }
}
