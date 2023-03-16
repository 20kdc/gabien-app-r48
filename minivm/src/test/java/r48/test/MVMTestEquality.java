/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.test;

import static org.junit.Assert.*;

import org.junit.Test;

import r48.minivm.MVMU;

/**
 * Created 16th March 2023
 */
public class MVMTestEquality {
    @Test
    public void testNumbers() {
        testAlways(MVMU::eqQ);
        testAlways(MVMU::eqvQ);
        testAlways(MVMU::equalQ);
        // list equality
        assertFalse(MVMU.eqQ(MVMU.l(1L, 2L, 3L), MVMU.l(1L, 2L, 3L)));
        assertFalse(MVMU.eqvQ(MVMU.l(1L, 2L, 3L), MVMU.l(1L, 2L, 3L)));
        assertTrue(MVMU.equalQ(MVMU.l(1L, 2L, 3L), MVMU.l(1L, 2L, 3L)));
        // list equality in the event of complexities
        assertTrue(MVMU.equalQ(MVMU.l(1L, 2L, 3L), MVMU.l(1L, 2L, 3L)));
    }
    public void testAlways(EqPredicate eqp) {
        // integer
        assertTrue(eqp.test(1L, 1L));
        assertFalse(eqp.test(2L, 1L));
        assertFalse(eqp.test(1L, 2L));
        // float
        assertTrue(eqp.test(1d, 1d));
        assertFalse(eqp.test(2d, 1d));
        assertFalse(eqp.test(1d, 2d));
        // two strings of different content are never equal
        assertFalse(eqp.test("A", "B"));
        // two characters of same content are always equal
        assertTrue(eqp.test('A', 'A'));
        // two characters of different content are always equal
        assertFalse(eqp.test('A', 'B'));
    }
    interface EqPredicate {
        boolean test(Object a, Object b);
    }
}
