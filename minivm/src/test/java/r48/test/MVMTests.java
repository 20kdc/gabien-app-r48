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
import r48.minivm.MVMU;
import r48.minivm.fn.MVMCoreLibraries;

/**
 * Created 1st March 2023
 */
public class MVMTests {
    public static MVMEnv prepEnv() {
        MVMEnv env = new MVMEnv();
        MVMCoreLibraries.add(env);
        return env;
    }
    public static void disasm(MVMEnv env, String p, String sym) {
        System.out.println(p + ": " + MVMU.userStr(env.evalString("(mvm-disasm " + sym + ")")));
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
        // Note the several layers of lambdas.
        // This is to keep an eye on the scoping.
        env.evalString("(define (testsym) (lambda () (define test 123) (lambda () test)))");
        disasm(env, "testLambdas", "testsym");
        assertEquals(123L, env.evalString("(((testsym)))"));
    }

    @Test
    public void testLambdasVA() {
        MVMEnv env = prepEnv();
        env.evalString("(define (list . v) v)");
        env.evalString("(define (va0 . a0) (list a0))");
        env.evalString("(define (va1 a0 . a1) (list a0 a1))");
        env.evalString("(define (va2 a0 a1 . a2) (list a0 a1 a2))");
        env.evalString("(define (va3 a0 a1 a2 . a3) (list a0 a1 a2 a3))");
        env.evalString("(define (va4 a0 a1 a2 a3 . a4) (list a0 a1 a2 a3 a4))");
        env.evalString("(define (va5 a0 a1 a2 a3 a4 . a5) (list a0 a1 a2 a3 a4 a5))");
        assertEquals(MVMU.l(MVMU.l(0L)), env.evalString("(va0 0)"));
        assertEquals(MVMU.l(0L, MVMU.l(1L)), env.evalString("(va1 0 1)"));
        assertEquals(MVMU.l(0L, 1L, MVMU.l(2L)), env.evalString("(va2 0 1 2)"));
        assertEquals(MVMU.l(0L, 1L, 2L, MVMU.l(3L)), env.evalString("(va3 0 1 2 3)"));
        assertEquals(MVMU.l(0L, 1L, 2L, 3L, MVMU.l(4L)), env.evalString("(va4 0 1 2 3 4)"));
        assertEquals(MVMU.l(0L, 1L, 2L, 3L, 4L, MVMU.l(5L)), env.evalString("(va5 0 1 2 3 4 5)"));
    }

    @Test
    public void testMacros() {
        MVMEnv env = prepEnv();
        env.evalString("(define (list . v) v)");
        env.evalString("(define-syntax (teu c b a) (list a b c))");
        env.evalString("(define (test-teu) (teu 6 5 +))");
        assertEquals(11L, env.evalString("(test-teu)"));
        disasm(env, "testMacros", "test-teu");
    }
}
