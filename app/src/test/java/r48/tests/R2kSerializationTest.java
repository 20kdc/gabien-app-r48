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
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.obj.FixedObjectProps;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.obj.MapUnit;
import r48.io.r2k.obj.Save;
import r48.io.r2k.obj.ldb.Database;
import r48.io.r2k.struct.MapTree;
import r48.io.undoredo.DMChangeTracker;
import r48.ioplus.Reporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    @Test
    public void testCreateAllReachableStructs() throws IOException {
        DMContext dmc = new DMContext(DMChangeTracker.Null.TESTS, StandardCharsets.UTF_8);
        Save save = new Save(dmc);
        MapUnit mu = new MapUnit(dmc);
        MapTree mt = new MapTree(dmc);
        Database db = new Database(dmc);
        createAllReachableStructsAndSave(save);
        createAllReachableStructsAndSave(mu);
        createAllReachableStructsAndSave(mt);
        createAllReachableStructsAndSave(db);
    }

    public void createAllReachableStructsAndSave(IR2kInterpretable obj) {
        createAllReachableStructs((IRIO) obj);
        try {
            obj.exportData(new ByteArrayOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAllReachableStructs(IRIO obj) {
        if (obj instanceof IRIOFixedObject) {
            IRIOFixedObject ifo = (IRIOFixedObject) obj;
            FixedObjectProps fops = new FixedObjectProps(ifo.getClass());
            for (FixedObjectProps.FXOBinding fb : fops.fxoBindingsArray)
                if (fb.optional)
                    ifo.addIVar(fb.iVar);
        }
        if (obj.getType() == '[') {
            while (obj.getALen() < 2)
                obj.addAElem(obj.getALen());
            for (int i = 0; i < obj.getALen(); i++)
                createAllReachableStructs(obj.getAElem(i));
        } else if (obj.getType() == '{' || obj.getType() == '}') {
            createAllReachableStructs(obj.addHashVal(DMKey.of(1)));
        }
        for (String s : obj.getIVars())
            createAllReachableStructs(obj.getIVar(s));
    }
}
