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
        assertFalse(MVMU.eqQ(MVMU.l(1, 2, 3), MVMU.l(1, 2, 3)));
        assertFalse(MVMU.eqvQ(MVMU.l(1, 2, 3), MVMU.l(1, 2, 3)));
        assertTrue(MVMU.equalQ(MVMU.l(1, 2, 3), MVMU.l(1, 2, 3)));
        // list equality in the event of complexities
        assertTrue(MVMU.equalQ(MVMU.l(1, 2, 3), MVMU.l(1L, 2L, 3L)));
    }
    public void testAlways(EqPredicate eqp) {
        // equality between all integer kinds of number is always present
        assertTrue(eqp.test(1, 1));
        assertTrue(eqp.test(1, 1L));
        assertTrue(eqp.test(1, (short) 1));
        assertTrue(eqp.test(1, (byte) 1));
        assertTrue(eqp.test(1, 1));
        assertTrue(eqp.test(1L, 1));
        assertTrue(eqp.test((short) 1, 1));
        assertTrue(eqp.test((byte) 1, 1));
        // nonequality between all integer kinds of number is always present
        assertFalse(eqp.test(2, 1));
        assertFalse(eqp.test(2, 1L));
        assertFalse(eqp.test(2, (short) 1));
        assertFalse(eqp.test(2, (byte) 1));
        assertFalse(eqp.test(1, 2));
        assertFalse(eqp.test(1L, 2));
        assertFalse(eqp.test((short) 1, 2));
        assertFalse(eqp.test((byte) 1, 2));
        // equality between all float kinds of number is always present
        assertTrue(eqp.test(1f, 1f));
        assertTrue(eqp.test(1f, 1d));
        assertTrue(eqp.test(1f, 1f));
        assertTrue(eqp.test(1d, 1f));
        // nonequality between all float kinds of number is always present
        assertFalse(eqp.test(2f, 1f));
        assertFalse(eqp.test(2f, 1d));
        assertFalse(eqp.test(1f, 2f));
        assertFalse(eqp.test(1d, 2f));
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
