/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;

import r48.R48;
import r48.io.IMIUtils;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.undoredo.DMChangeTracker;
import r48.schema.util.SchemaPath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created on December 09, 2018.
 */
public class IMIBasicTest {
    @Test
    public void testEncode2kDatabase() {
        DMContext tests = new DMContext(DMChangeTracker.Null.TESTS, StandardCharsets.UTF_8);
        R48 app = new TestKickstart().kickstart("RAM/", "UTF-8", "r2k");
        // Use RubyIOs both in and out to deal with encoding oddities
        IRIO newObj = new IRIOGeneric(tests);
        SchemaPath.setDefaultValue(newObj, app.sdb.getSDBEntry("RPG::Database"), null);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IMIUtils.createIMIDump(new DataOutputStream(baos), newObj, "");
            IRIOGeneric test = new IRIOGeneric(tests);
            byte[] data = baos.toByteArray();
            IMIUtils.runIMISegment(new ByteArrayInputStream(data), test);
            byte[] diff = IMIUtils.createIMIData(newObj, test, "");
            if (diff != null) {
                System.out.println("-- Dbg : Data");
                System.out.println(new String(data, "UTF-8"));
                System.out.println("-- Dbg : Diff");
                System.out.println(new String(diff, "UTF-8"));
                throw new RuntimeException("There was an issue.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Possibly compare objects in IMI?
    }
}
