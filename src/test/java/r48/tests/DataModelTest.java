/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests;

import org.junit.Assert;
import org.junit.Test;
import r48.RubyIO;

/**
 * Created on November 19, 2018.
 */
public class DataModelTest {
    @Test
    public void testStringEquality() {
        // This is really just to make sure unit tests work for now.
        RubyIO rioA = new RubyIO().setString("Hello", true);
        RubyIO rioB = new RubyIO().setString("Goodbye", true);
        RubyIO rioC = new RubyIO().setString("Hello", true);
        Assert.assertFalse(RubyIO.rubyEquals(rioA, rioB));
        Assert.assertTrue(RubyIO.rubyEquals(rioA, rioA));
        Assert.assertTrue(RubyIO.rubyEquals(rioA, rioC));
    }
}
