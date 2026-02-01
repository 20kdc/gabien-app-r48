/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.undoredo.DMChangeTracker;

/**
 * Created on November 19, 2018.
 */
public class DataModelTest {
    @Test
    public void testStringEquality() {
        DMContext tests = new DMContext(DMChangeTracker.Null.TESTS, StandardCharsets.UTF_8);
        // This is really just to make sure unit tests work for now.
        IRIO rioA = new IRIOGeneric(tests).setString("Hello");
        IRIO rioB = new IRIOGeneric(tests).setString("Goodbye");
        IRIO rioC = new IRIOGeneric(tests).setString("Hello");
        Assert.assertFalse(IRIO.rubyEquals(rioA, rioB));
        Assert.assertTrue(IRIO.rubyEquals(rioA, rioA));
        Assert.assertTrue(IRIO.rubyEquals(rioA, rioC));
    }
}
