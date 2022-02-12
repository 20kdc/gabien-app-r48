/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import r48.RubyIO;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TestDBUtils;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.SchemaPath;

import java.io.IOException;
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
        TestKickstart.kickstartRFS();
        final HashSet<String> schemas = new HashSet<String>();
        DBLoader.readFile("Gamepaks.txt", new IDatabase() {
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
        TestKickstart.kickstart("RAM/", "UTF-8", gamepak + "/");
        // ... Also does this.
        // Not really parsing, but a good safety measure none-the-less.
        for (EventCommandArraySchemaElement st : TestDBUtils.getLoadedCSLs()) {
            final RubyIO rio = new RubyIO();
            SchemaPath.setDefaultValue(rio, st, null);
            RubyIO rio2 = rio.addAElem(0);
            SchemaPath.setDefaultValue(rio2, st.baseElement, new IRIOFixnum(0));
            for (int i : st.database.knownCommandOrder) {
                rio2.getIVar("@code").setFX(i);
                st.baseElement.modifyVal(rio, new SchemaPath(st, new IObjectBackend.MockLoadedObject(rio)), false);
            }
        }
    }
}
