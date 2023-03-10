/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tests;

import gabien.TestKickstart;
import gabien.uslx.append.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import r48.App;
import r48.dbs.DBLoader;
import r48.dbs.ObjectInfo;
import r48.dbs.IDatabase;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The Local Test Executive (LTE) is responsible for executing tests containing copyrighted data
 * that is not public-domain and thus cannot be part of the R48 repository.
 * Created on December 08, 2018.
 */
@RunWith(Parameterized.class)
public class LocalTestExecutiveTest {
    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        final LinkedList<Object[]> tests = new LinkedList<Object[]>();
        TestKickstart.kickstartRFS();
        try {
            DBLoader.readFile(null, "LTE.txt", new IDatabase() {
                @Override
                public void newObj(int objId, final String objName) {
                }

                @Override
                public void execCmd(char c, String[] args) {
                    if (c == '.') {
                        Object[] whiteLight = new Object[args.length];
                        for (int i = 0; i < whiteLight.length; i++)
                            whiteLight[i] = args[i];
                        tests.add(whiteLight);
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Exception during LocalTestExecutive parameterization. Unread tests will not be executed.");
            System.err.println("If you do not have the LTE files, then this is normal, do not panic.");
            e.printStackTrace();
        }
        return tests;
    }

    private final String name;
    @SuppressWarnings("unused")
    private final String friendlyName, schema, charset;
    private final boolean dynamic;

    public LocalTestExecutiveTest(String nam, String friendlyNam, String sc, String charse, String dyn) {
        name = nam;
        friendlyName = friendlyNam;
        schema = sc;
        charset = charse;
        dynamic = Boolean.valueOf(dyn);
    }

    @Test
    public void test() {
        App app = TestKickstart.kickstart(name + "/", charset, schema);
        for (ObjectInfo s : dynamic ? app.getObjectInfos() : app.sdb.listFileDefs())
            testObject(app, s.idName);
    }

    private void testObject(App app, String s) {
        try {
            System.out.println(s);

            // 'objectUnderTest' is the reference copy. DO NOT ALTER IT UNTIL THE END.
            IObjectBackend.ILoadedObject objectUnderTest = app.odb.getObject(s, null);

            // Create an internal copy, autocorrect it, save it, and then get rid of it.
            {
                IObjectBackend.ILoadedObject objectInternalCopy = app.odb.backend.newObject(s);

                objectInternalCopy.getObject().setDeepClone(objectUnderTest.getObject());

                SchemaElement wse = findSchemaFor(app, s, objectUnderTest.getObject());
                app.odb.registerModificationHandler(objectInternalCopy, new IConsumer<SchemaPath>() {
                    @Override
                    public void accept(SchemaPath schemaPath) {
                        throw new RuntimeException("A modification occurred on LTE data. This shouldn't happen: " + schemaPath.toString());
                    }
                });
                wse.modifyVal(objectInternalCopy.getObject(), new SchemaPath(wse, objectInternalCopy), false);

                objectInternalCopy.save();

                // This is to TRY to get 'objectInternalCopy' out of memory.
                objectInternalCopy = null;
                System.gc();
            }

            // Load the autocorrected + saved object, and see if anything went bang.
            {
                IObjectBackend.ILoadedObject objectSaveLoaded = app.odb.backend.loadObject(s);
                byte[] bytes = IMIUtils.createIMIData(objectUnderTest.getObject(), objectSaveLoaded.getObject(), "");
                if (bytes != null) {
                    System.out.write(bytes);
                    System.out.flush();
                    throw new RuntimeException("Difference found.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SchemaElement findSchemaFor(App app, String s, IRIO object) {
        if (app.sdb.hasSDBEntry("File." + s))
            return app.sdb.getSDBEntry("File." + s);
        if (object.getType() == 'o')
            return app.sdb.getSDBEntry(object.getSymbol());
        throw new RuntimeException("Unable to find schema for tested object " + s + ". Add hard-coded test setup.");
    }

}
