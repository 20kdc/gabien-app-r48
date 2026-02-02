/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Assert;
import org.junit.Test;

import r48.R48;
import r48.dbs.ObjectRootHandle;
import r48.ioplus.Reporter;

import java.io.IOException;

/**
 * Created on December 02, 2018.
 */
public class R2kSerializationTest {
    @Test
    public void testFullIOStack() throws IOException {
        TestKickstart kick = new TestKickstart();
        R48 app = kick.kickstart("RAM/", "UTF-8", "r2k");

        String[] fileDefs = new String[] {
                "hello.lmu",
                "world.ldb",
                "and.lmt",
                "you.lsd",
        };
        // Save it, but skip past most of ObjectDB since it will not be queried in future & it uses UI on failure.
        Reporter dummy = new Reporter.Dummy(app.t);
        app.odb.getObject("hello.lmu", true).ensureSaved(dummy);
        app.odb.getObject("world.ldb", true).ensureSaved(dummy);
        app.odb.getObject("and.lmt", true).ensureSaved(dummy);
        app.odb.getObject("you.lsd", true).ensureSaved(dummy);
        // Kills off the old ObjectDB
        kick.resetODB(app);

        for (String s : fileDefs) {
            ObjectRootHandle i = app.odb.getObject(s, false);
            Assert.assertNotNull(i);
        }
    }
}
