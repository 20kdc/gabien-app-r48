/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;

import r48.App;
import r48.io.IMIUtils;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.undoredo.DMChangeTracker;
import r48.schema.util.SchemaPath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created on November 21, 2018.
 */
public class DeepCloneTest {
    @Test
    public void testDeepCloneRPGMap() {
        DMContext tests = new DMContext(DMChangeTracker.Null.TESTS, StandardCharsets.UTF_8);
        App app = new TestKickstart().kickstart("RAM/", "UTF-8", "rxp");
        IRIO newObj = new IRIOGeneric(tests);
        SchemaPath.setDefaultValue(newObj, app.sdb.getSDBEntry("RPG::Map"), null);
        IRIO newObj2 = new IRIOGeneric(tests).setDeepClone(newObj);
        try {
            byte[] dat = IMIUtils.createIMIData(newObj, newObj2, "");
            if (dat != null)
                System.out.write(dat);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Possibly compare objects in IMI?
    }
}
