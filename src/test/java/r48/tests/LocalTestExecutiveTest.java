package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.map.systems.IDynobjMapSystem;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The Local Test Executive (LTE) is responsible for executing tests containing copyrighted data
 * that is not available under CC0 and thus cannot be part of the R48 repository.
 * Created on December 08, 2018.
 */
@RunWith(Parameterized.class)
public class LocalTestExecutiveTest {
    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        final LinkedList<Object[]> tests = new LinkedList<Object[]>();
        TestKickstart.kickstartRFS();
        DBLoader.readFile("LTE.txt", new IDatabase() {
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
        return tests;
    }

    private final String name, friendlyName, schema, charset;

    public LocalTestExecutiveTest(String nam, String friendlyNam, String sc, String charse) {
        name = nam;
        friendlyName = friendlyNam;
        schema = sc;
        charset = charse;
    }

    @Test
    public void test() {
        TestKickstart.kickstart(name + "/", charset, schema + "/");
        for (String s : AppMain.schemas.listFileDefs())
            testObject(s);
        if (AppMain.system instanceof IDynobjMapSystem)
            for (String s : ((IDynobjMapSystem) AppMain.system).getDynamicObjects())
                testObject(s);
    }

    private void testObject(String s) {
        System.out.println(s);
        IObjectBackend.ILoadedObject i = AppMain.objectDB.getObject(s, null);
        RubyIO conv = new RubyIO().setDeepClone(i.getObject());
        try {
            byte[] bytes = IMIUtils.createIMIData(i.getObject(), conv, "");
            if (bytes != null) {
                System.out.write(bytes);
                System.out.flush();
                throw new RuntimeException("Difference found.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        i.getObject().setDeepClone(conv);
        try {
            i.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
