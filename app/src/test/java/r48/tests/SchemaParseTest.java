/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import r48.App;
import r48.RubyIO;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TestDBUtils;
import r48.io.IObjectBackend;
import r48.io.data.IRIOFixnum;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.SchemaPath;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Ensures that all schemas parse correctly.
 * Created on November 19, 2018.
 */
@RunWith(Parameterized.class)
public class SchemaParseTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        LinkedList<Object[]> tests = new LinkedList<Object[]>();
        new TestKickstart().kickstartRFS();
        final HashSet<String> schemas = new HashSet<String>();
        DBLoader.readFile(null, "Gamepaks.txt", new IDatabase() {
            @Override
            public void newObj(int objId, final String objName) {
                schemas.add(objName);
            }

            @Override
            public void execCmd(char c, String[] args) {

            }
        });
        for (String st : schemas)
            tests.add(new Object[] {st});
        return tests;
    }

    private final String gamepak;

    public SchemaParseTest(String gp) {
        gamepak = gp;
    }

    @Test
    public void testParses() {
        System.out.println("SchemaParseTest START: " + gamepak);
        App app = new TestKickstart().kickstart("RAM/", "UTF-8", gamepak);
        // ... Also does this.
        // Not really parsing, but a good safety measure none-the-less.
        for (EventCommandArraySchemaElement st : TestDBUtils.getLoadedCSLs(app)) {
            final RubyIO rio = new RubyIO();
            SchemaPath.setDefaultValue(rio, st, null);
            RubyIO rio2 = rio.addAElem(0);
            SchemaPath.setDefaultValue(rio2, st.baseElement, new IRIOFixnum(0));
            for (int i : st.database.knownCommandOrder) {
                rio2.getIVar("@code").setFX(i);
                st.baseElement.modifyVal(rio, new SchemaPath(st, new IObjectBackend.MockLoadedObject(rio)), false);
            }
        }
        System.out.println("SchemaParseTest END: " + gamepak);
    }
}
