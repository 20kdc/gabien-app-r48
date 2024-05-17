/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.test;

import static org.junit.Assert.*;

import org.junit.Test;

import r48.minivm.MVMType;

/**
 * Created 17th May, 2024.
 */
public class MVMTypeSystemTest {
    @Test
    public void testObvious() {
        assertTrue(MVMType.OBJ.canImplicitlyCastFrom(MVMType.LIST));
        assertTrue(MVMType.OBJ.canImplicitlyCastFrom(MVMType.ANY));
        assertFalse(MVMType.OBJ.canImplicitlyCastFrom(MVMType.NULL));

        assertFalse(MVMType.LIST.canImplicitlyCastFrom(MVMType.OBJ));
        assertTrue(MVMType.LIST.canImplicitlyCastFrom(MVMType.ANY));
        assertFalse(MVMType.LIST.canImplicitlyCastFrom(MVMType.NULL));

        assertTrue(MVMType.ANY.canImplicitlyCastFrom(MVMType.OBJ));
        assertTrue(MVMType.ANY.canImplicitlyCastFrom(MVMType.LIST));
        assertTrue(MVMType.ANY.canImplicitlyCastFrom(MVMType.NULL));

        assertFalse(MVMType.NULL.canImplicitlyCastFrom(MVMType.OBJ));
        assertFalse(MVMType.NULL.canImplicitlyCastFrom(MVMType.LIST));
        assertTrue(MVMType.NULL.canImplicitlyCastFrom(MVMType.ANY));

        assertTrue(new MVMType.TypedList(MVMType.I64).canImplicitlyCastTo(MVMType.LIST));
    }
}
