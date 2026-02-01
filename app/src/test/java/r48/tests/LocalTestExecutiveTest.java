/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tests;

import gabien.TestKickstart;
import gabien.uslx.append.Block;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import datum.DatumReaderTokenSource;
import datum.DatumSrcLoc;
import datum.DatumTreeUtils;
import r48.App;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.data.DMContext;
import r48.io.undoredo.DMChangeTracker;
import r48.io.undoredo.IDM3Data;
import r48.ioplus.IDatabase;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        new TestKickstart().kickstartRFS();
        try {
            String fn = findBasePath() + "LTE.scm";
            System.out.println("LocalTestExecutive manifest: " + fn);
            try (InputStreamReader ins = new InputStreamReader(new FileInputStream(fn))) {
                DatumReaderTokenSource drts = new DatumReaderTokenSource(fn, ins);
                drts.visit(DatumTreeUtils.decVisitor(new IDatabase() {
                    @Override
                    public void newObj(int objId, final String objName, DatumSrcLoc sl) {
                    }

                    @Override
                    public void execCmd(String c, String[] args, Object[] argsObj, DatumSrcLoc sl) {
                        if (c.equals(".")) {
                            Object[] cmdLine = new Object[args.length];
                            for (int i = 0; i < cmdLine.length; i++)
                                cmdLine[i] = args[i];
                            tests.add(cmdLine);
                        } else {
                            throw new RuntimeException("unknown command: " + c);
                        }
                    }
                }));
           }
        } catch (Exception e) {
            System.err.println("Exception during LocalTestExecutive parameterization. Unread tests will not be executed.");
            System.err.println("If you do not have the LTE files, then this is normal, do not panic.");
            e.printStackTrace();
        }
        return tests;
    }

    public static String findBasePath() {
        try {
            String home = System.getenv("HOME");
            return home + "/R48LTE/assets/";
        } catch (Exception ex) {
            return "/R48LTE/assets/";
        }
    }

    private final String name;
    @SuppressWarnings("unused")
    private final String friendlyName, schema, charset;
    private final boolean dynamic;

    public LocalTestExecutiveTest(String nam, String friendlyNam, String sc, String charse, String dyn) {
        name = findBasePath() + nam + "/";
        friendlyName = friendlyNam;
        schema = sc;
        charset = charse;
        dynamic = Boolean.valueOf(dyn);
    }

    @Test
    public void test() {
        App app = new TestKickstart().kickstart("/real_fs" + name, charset, schema);
        for (ObjectInfo s : dynamic ? app.getObjectInfos() : app.sdb.listFileDefs()) {
            if (s.idName.equals("zR48ProjectConfig"))
                continue;
            testObject(app, s.idName);
        }
    }

    private void testObject(App app, String s) {
        try {
            System.out.print(s);

            // 'objectUnderTest' is the reference copy. DO NOT ALTER IT UNTIL THE END.
            ObjectRootHandle objectUnderTest = app.odb.getObject(s, false);
            if (objectUnderTest == null)
                throw new RuntimeException("Object get failure: " + s);
            if (objectUnderTest.rootSchema == null)
                throw new RuntimeException("Object schema failure: " + s);

            DMContext tests = new DMContext(DMChangeTracker.Null.TESTS, app.encoding);
            // Create an internal copy, autocorrect it, save it, and then get rid of it.
            {
                // in theory, we don't need an unpack license for tests, nor do we care
                // **HOWEVER:** since we're doing a deep clone, we 'might as well' test state copies!
                final LinkedList<Runnable> states = new LinkedList<>();
                DMContext monitor = new DMContext(new DMChangeTracker() {
                    @Override
                    public void register(IDM3Data irioData) {
                        irioData.trackingMarkClean();
                    }

                    @Override
                    public void modifying(IDM3Data modifiedData) {
                        states.add(modifiedData.saveState());
                    }
                }, app.encoding);
                ObjectRootHandle objectInternalCopy;
                IObjectBackend.ILoadedObject objectInternalCopyILO;
                try (Block license = monitor.changes.openUnpackLicense()) {
                    objectInternalCopyILO = app.odb.backend.newObject(s, monitor);
                    objectInternalCopy = new ObjectRootHandle.Isolated(objectUnderTest.rootSchema, objectInternalCopyILO.getObject(), "objectInternalCopy");
                }

                // should trigger lots of savestates which will help debug if there are any exceptions lurking
                objectInternalCopy.getObject().setDeepClone(objectUnderTest.getObject());
                int firstStates = states.size();
                System.out.println(" " + firstStates + " savestates");

                SchemaElement wse = SchemaElement.cast(objectUnderTest.rootSchema);
                objectInternalCopy.registerModificationHandler((schemaPath) -> {
                    throw new RuntimeException("A modification occurred on LTE data. This shouldn't happen: " + schemaPath.toString());
                });
                wse.modifyVal(objectInternalCopy.getObject(), new SchemaPath(wse, objectInternalCopy), false);

                objectInternalCopyILO.save();

                // now test the states
                for (Runnable r : states) {
                    r.run();
                }
                int secondStates = states.size();
                if (firstStates != secondStates)
                    throw new RuntimeException("a modification happened during schema AC and snuck past the handler");
            }
            // This is to TRY to get 'objectInternalCopy' out of memory.
            System.gc();

            // Load the autocorrected + saved object, and see if anything went bang.
            {
                // we don't need an unpack license for tests
                IObjectBackend.ILoadedObject objectSaveLoaded = app.odb.backend.loadObject(s, tests);
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
}
