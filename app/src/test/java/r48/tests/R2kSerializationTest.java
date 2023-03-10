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

import r48.App;
import r48.io.IObjectBackend;

import java.io.IOException;

/**
 * Created on December 02, 2018.
 */
public class R2kSerializationTest {
    @Test
    public void testFullIOStack() throws IOException {
        App app = TestKickstart.kickstart("RAM/", "UTF-8", "r2k");

        String[] fileDefs = new String[] {
                "hello.lmu",
                "world.ldb",
                "and.lmt",
                "you.lsd",
        };
        // Save it, but skip past most of ObjectDB since it will not be queried in future & it uses UI on failure.
        app.odb.getObject("hello.lmu", "RPG::Map").save();
        app.odb.getObject("world.ldb", "RPG::Database").save();
        app.odb.getObject("and.lmt", "RPG::MapTree").save();
        app.odb.getObject("you.lsd", "RPG::Save").save();
        // Kills off the old ObjectDB
        TestKickstart.resetODB(app);

        for (String s : fileDefs) {
            IObjectBackend.ILoadedObject i = app.odb.getObject(s, null);
            Assert.assertNotNull(i);
        }
    }
}
